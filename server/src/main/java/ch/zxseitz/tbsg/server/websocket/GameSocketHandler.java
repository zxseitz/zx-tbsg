package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.ClientException;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.server.games.GameProxy;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class GameSocketHandler extends TextWebSocketHandler {
    private final Logger logger;
    private final GameProxy proxy;
    private final Map<String, Client> clients;

    public GameSocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.clients = new ConcurrentHashMap<>();
    }

    /**
     * Performs a critical section on several locks
     *
     * @param callable critical section
     * @param locks list of lock
     * @return critical section return value
     * @throws Exception if an exception occurs during the critical section
     */
    @SafeVarargs
    private static <T, L> T critical(Callable<T> callable, ILockable<L>... locks) throws Exception {
        var sorted = Arrays.stream(locks).sorted().collect(Collectors.toList());  //prevent deadlocks
        sorted.forEach(ILockable::lock);
        try {
            return callable.call();
        } finally {
            sorted.forEach(ILockable::unlock);
        }
    }

    /**
     * Handles client connection
     *
     * @param session client websocket session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New websocket client: {}", session.getId());
        // todo: read auth infos
        var client = new Client(session);
        clients.put(session.getId(), client);
    }

    /**
     * Handles client disconnection
     *
     * @param session client websocket session
     * @param status close status
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Websocket client disconnected: {}", session.getId());
        var client = clients.remove(session.getId());
        try {
            critical(() -> {
                // abort challenges
                client.getChallenges().forEach(client1 -> {
                    try {
                        client.invoke(EventManager.createChallengeAbortEvent(client));
                    } catch (ClientException ignored) {
                    }
                });
                // abort match
                var match = client.getMatch();
                if (match != null) {
                    var members = (Client[]) critical(() -> {
                        match.get().resign(client);
                        return match.get().getClients();
                    }, match);  // return match lock to prevent deadlock with game events
                    for (var member : members) {
                        critical(() -> {
                            member.setMatch(null);
                            return null;
                        }, member);
                    }
                }
                return null;
            }, client);
        } catch (Exception ignored) {

        }
    }

    /**
     * Handles client events
     *
     * @param session client websocket session
     * @param message client event
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        var client = clients.get(session.getId());
        try {
            var event = EventManager.parse(message.getPayload());
            switch (event.getCode()) {
                case EventManager.CODE_CHALLENGE: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        client.getChallenges().add(opponent);
                        opponent.invoke(EventManager.createChallengeEvent(client));
                        return null;
                    }, client);
                    break;
                }
                case EventManager.CODE_CHALLENGE_ABORT: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        if (client.getChallenges().remove(opponent)) {
                            opponent.invoke(EventManager.createChallengeAbortEvent(client));
                        } else {
                            throw new TbsgException(String.format("Opponent [%s] is not challenged by you", opponentId));
                        }
                        return null;
                    }, client);
                    break;
                }
                case EventManager.CODE_CHALLENGE_ACCEPT: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        if (client.getMatch() == null && opponent.getMatch() == null) {
                            List<IClient> clients = Arrays.asList(client, opponent);
                            var match = proxy.getInstance().createMatch(clients);
                            var matchLocker = new Locker<>(match);
                            client.setMatch(matchLocker);
                            opponent.setMatch(matchLocker);
                            match.init();
                        } else {
                            throw new TbsgException(String.format("You or opponent [%s] is currently in game", opponentId));
                        }
                        return null;
                    }, client, opponent);
                    break;
                }
                case EventManager.CODE_CHALLENGE_DECLINE: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        if (opponent.getChallenges().remove(client)) {
                            opponent.invoke(EventManager.createChallengeDeclineEvent(client));
                        } else {
                            throw new TbsgException(String.format("Opponent [%s] is not challenged by you", opponentId));
                        }
                        return null;
                    }, opponent);
                    break;
                }
                case EventManager.CODE_LIST: {
                    var idNames = new HashMap<String, String>();
                    clients.values().forEach(client1 -> {
                            idNames.put(client1.getId(), client1.getName());
                    });
                    idNames.remove(client.getId());
                    client.invoke(EventManager.createChallengeListEvent(idNames));
                    break;
                }
                default:
                    critical(() -> {
                        var match = client.getMatch();
                        if (match != null) {
                            critical(() -> {
                                match.get().invoke(client, event);
                                return null;
                            }, match);
                        } else {
                            throw new TbsgException("You are not in game to invoke this event");
                        }
                        return null;
                    }, client);
            }
        } catch (Exception e) {
            try {
                client.invoke(EventManager.createErrorEvent(e.getMessage()));
            } catch (ClientException ce) {
                logger.warn(ce.getMessage(), ce);
            }
        }
    }
}

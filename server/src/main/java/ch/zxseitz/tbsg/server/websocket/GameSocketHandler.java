package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.ClientException;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.server.games.GameProxy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private final Map<String, Locker<Client>> clients;

    public GameSocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.clients = new ConcurrentHashMap<>();
    }

    private static <T> T critical(Callable<T> callable, Locker<?>... locks) throws Exception {
        var sorted = Arrays.stream(locks).sorted().collect(Collectors.toList());  //prevent deadlocks
        sorted.forEach(Locker::lock);
        try {
            return callable.call();
        } finally {
            sorted.forEach(Locker::unlock);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New websocket client: {}", session.getId());
        // todo: read auth infos
        var client = new Client(session);
        clients.put(session.getId(), new Locker<>(client));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Websocket client disconnected: {}", session.getId());
        var clientLocker = clients.remove(session.getId());
        try {
            critical(() -> {
                var client = clientLocker.get();
                // abort challenges
                client.getChallenges().forEach(client1 -> {
                    try {
                        client1.get().invoke(EventManager.createChallengeAbortEvent(client));
                    } catch (ClientException ignored) {
                    }
                });
                // abort match
                var matchLocker = client.getMatch();
                if (matchLocker != null) {
                    var members = critical(() -> {
                        var match = matchLocker.get();
                        match.resign(client);
                        return match.getClients();
                    }, matchLocker);
                    for (var member : members) {
                        var memberLock = clients.get(member.getId());
                        if (memberLock != null) {
                            critical(() -> {
                                memberLock.get().setMatch(null);
                                return null;
                            }, memberLock);
                        }
                    }
                }
                return null;
            }, clientLocker);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        var clientLocker = clients.get(session.getId());
        try {
            var event = EventManager.parse(message.getPayload());
            switch (event.getCode()) {
                case EventManager.CODE_CHALLENGE: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponentLocker = clients.get(opponentId);
                    if (opponentLocker == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        var client = clientLocker.get();
                        client.getChallenges().add(opponentLocker);
                        opponentLocker.get().invoke(EventManager.createChallengeEvent(client));
                        return null;
                    }, clientLocker);
                    break;
                }
                case EventManager.CODE_CHALLENGE_ABORT: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        var client = clientLocker.get();
                        if (client.getChallenges().remove(opponent)) {
                            opponent.get().invoke(EventManager.createChallengeAbortEvent(client));
                        } else {
                            throw new TbsgException(String.format("Opponent [%s] is not challenged by you", opponentId));
                        }
                        return null;
                    }, clientLocker);
                    break;
                }
                case EventManager.CODE_CHALLENGE_ACCEPT: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponentLocker = clients.get(opponentId);
                    if (opponentLocker == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        var client = clientLocker.get();
                        var opponent = opponentLocker.get();
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
                    }, clientLocker, opponentLocker);
                    break;
                }
                case EventManager.CODE_CHALLENGE_DECLINE: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponentLocker = clients.get(opponentId);
                    if (opponentLocker == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    critical(() -> {
                        var opponent = opponentLocker.get();
                        if (opponent.getChallenges().remove(clientLocker)) {
                            opponent.invoke(EventManager.createChallengeDeclineEvent(clientLocker.get()));
                        } else {
                            throw new TbsgException(String.format("Opponent [%s] is not challenged by you", opponentId));
                        }
                        return null;
                    }, opponentLocker);
                    break;
                }
                default:
                    critical(() -> {
                        var matchLocker = clientLocker.get().getMatch();
                        if (matchLocker != null) {
                            critical(() -> {
                                matchLocker.get().invoke(clientLocker.get(), event);
                                return null;
                            }, matchLocker);
                        } else {
                            throw new TbsgException("You are not in game to invoke this event");
                        }
                        return null;
                    }, clientLocker);
            }
        } catch (Exception e) {
            try {
                clientLocker.get().invoke(EventManager.createErrorEvent(e.getMessage()));
            } catch (ClientException ce) {
                logger.warn(ce.getMessage(), ce);
            }
        }
    }
}

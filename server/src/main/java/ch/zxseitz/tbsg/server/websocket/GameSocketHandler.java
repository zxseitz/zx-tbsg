package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.ClientException;
import ch.zxseitz.tbsg.server.games.GameProxy;

import java.util.Arrays;
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
    private final Map<String, Client> clients;

    public GameSocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.clients = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New websocket client: {}", session.getId());
        // todo: read auth infos
        var client = new Client(session);
        clients.put(session.getId(), client);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Websocket client disconnected: {}", session.getId());
        var client = clients.remove(session.getId());
        if (client != null) {
            //todo abort challenges and matches
        }
    }

    private <T> T critical(Callable<T> callable, Client... clients) throws Exception {
        var locks = Arrays.stream(clients).sorted().collect(Collectors.toList());  //prevent deadlocks
        locks.forEach(Client::lock);
        try {
            return callable.call();
        } finally {
            locks.forEach(Client::unlock);
        }
    }

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
                        throw new TbsgException("Opponent [%s] is not connected");
                    }
                    critical(() -> {
                        client.getChallenges().add(opponent);
                        opponent.invoke(EventManager.createChallengeEvent(client));
                        return null;
                    }, client, opponent);
                    break;
                }
                case EventManager.CODE_CHALLENGE_ABORT: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException("Opponent [%s] is not connected");
                    }
                    critical(() -> {
                        if (client.getChallenges().remove(opponent)) {
                            opponent.invoke(EventManager.createChallengeAbortEvent(client));
                        } else {
                            throw new TbsgException("Opponent [%s] is not challenged by you");
                        }
                        return null;
                    }, client, opponent);
                    break;
                }
                case EventManager.CODE_CHALLENGE_ACCEPT: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException("Opponent [%s] is not connected");
                    }
                    critical(() -> {
                        // todo create match
                        return null;
                    }, client, opponent);
                    break;
                }
                case EventManager.CODE_CHALLENGE_DECLINE: {
                    var opponentId = event.getArgument("opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException("Opponent [%s] is not connected");
                    }
                    critical(() -> {
                        if (opponent.getChallenges().remove(client)) {
                            opponent.invoke(EventManager.createChallengeDeclineEvent(client));
                        } else {
                            throw new TbsgException("Opponent [%s] is not challenged by you");
                        }
                        return null;
                    }, client, opponent);
                    break;
                }
                default:
                    //todo invoke game events
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

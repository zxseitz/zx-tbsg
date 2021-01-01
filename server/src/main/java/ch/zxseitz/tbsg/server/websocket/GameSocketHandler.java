package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.IMatch;
import ch.zxseitz.tbsg.server.games.GameProxy;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, IMatch> matches;

    public GameSocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.clients = new ConcurrentHashMap<>();
        this.matches = new ConcurrentHashMap<>();
    }

    private void criticalPlayerSection(Callable<Void> action, Client... clients) throws Exception {
        var stream = Arrays.stream(clients).sorted();  // prevent deadlocks
        stream.forEach(Client::lock);
        try {
            action.call();
        } finally {
            stream.forEach(Client::unlock);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var client = clients.get(session.getId());
        var event = EventManager.parse(message.getPayload());

        switch (event.getCode()) {
            case EventManager.CODE_CHALLENGE: {
                var opponentId = event.getArgument("opponent", String.class);
                if (opponentId != null) {
                    var opponent = clients.get(opponentId);
                    if (opponent != null) {
                        criticalPlayerSection(() -> {
                            client.getChallenges().add(opponent);
                            opponent.send(EventManager.stringify(EventManager.CODE_CHALLENGE,
                                    Map.entry("opponent", client.getID())));
                            return null;
                        }, client, opponent);
                    }
                }
                break;
            }
            case EventManager.CODE_CHALLENGE_ABORT: {
                var opponentId = event.getArgument("opponent", String.class);
                if (opponentId != null) {
                    var opponent = clients.get(opponentId);
                    if (opponent != null) {
                        criticalPlayerSection(() -> {
                            if (client.getChallenges().remove(opponent)) {
                                opponent.send(EventManager.stringify(EventManager.CODE_CHALLENGE_ABORT,
                                        Map.entry("opponent", client.getID())));
                            }
                            return null;
                        }, client, opponent);
                    }
                }
                break;
            }
            case EventManager.CODE_CHALLENGE_ACCEPT: {
                //todo init match
                break;
            }
            case EventManager.CODE_CHALLENGE_DECLINE: {
                var opponentId = event.getArgument("opponent", String.class);
                if (opponentId != null) {
                    var opponent = clients.get(opponentId);
                    if (opponent != null) {
                        criticalPlayerSection(() -> {
                            if (opponent.getChallenges().remove(client)) {
                                opponent.send(EventManager.stringify(EventManager.CODE_CHALLENGE_DECLINE,
                                        Map.entry("opponent", client.getID())));
                            }
                            return null;
                        }, client, opponent);
                    }
                }
                break;
            }
            default:
                //todo game events
                client.send(EventManager.stringify(0,
                        Map.entry("error", "unknown event code" + event.getCode())));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New websocket client: {}", session.getId());
        // todo: read auth infos
        var player = new Client(session);
        clients.put(session.getId(), player);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Websocket client disconnected: {}", session.getId());
        var player = clients.remove(session.getId());
        //todo handle disconnect
    }
}

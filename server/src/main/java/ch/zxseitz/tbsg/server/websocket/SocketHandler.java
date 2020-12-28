package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.server.games.GameProxy;
import ch.zxseitz.tbsg.server.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketHandler extends TextWebSocketHandler {
    private final Logger logger;
    private final GameProxy proxy;
    private final Map<WebSocketSession, User> sessions;

    public SocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(SocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        // todo validate message
        // todo subscribe session to game
        var objectMapper = new ObjectMapper();
        var value = objectMapper.readTree(message.getPayload());
        var instance = proxy.getInstance();
        var response = instance.invoke(value.get("name").asText());
        session.sendMessage(new TextMessage(response));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New websocket client: {}", session.getId());
        // todo: read auth infos
        sessions.put(session, User.GUEST);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Websocket client disconnected: {}", session.getId());
        sessions.remove(session);
    }
}

//the messages will be broadcast to all users.
/*for(WebSocketSession webSocketSession : sessions) {
	Map value = new Gson().fromJson(message.getPayload(), Map.class);
	webSocketSession.sendMessage(new TextMessage("Hello " + value.get("name") + " !"));
}*/

package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.server.games.GameProxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.function.BiConsumer;

public class GameSocketHandler extends TextWebSocketHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger;
    private final GameProxy proxy;
    private final Lobby lobby;
    private final Map<Integer, BiConsumer<Client, ObjectNode>> clientEvents;

    public GameSocketHandler(Lobby lobby, GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.lobby = lobby;
        this.clientEvents = new TreeMap<>();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var client = lobby.getClients().get(session.getId());
        var node = mapper.readTree(message.getPayload());
        var codeNode = node.get("code");
        var argsNode = node.get("args");
        if (codeNode != null && codeNode.isInt() && argsNode != null && argsNode.isObject()) {
            var clientEvent = clientEvents.get(codeNode.intValue());
            if (clientEvent != null) {
                clientEvent.accept(client, (ObjectNode) argsNode);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New websocket client: {}", session.getId());
        // todo: read auth infos
        var client = new Client(session);
        lobby.getClients().putIfAbsent(session.getId(), client);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Websocket client disconnected: {}", session.getId());
        var client = lobby.getClients().remove(session.getId());
        //todo abort challenges and matches
    }
}

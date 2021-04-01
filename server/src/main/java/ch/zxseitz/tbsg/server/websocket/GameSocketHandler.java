package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.exceptions.ClientException;
import ch.zxseitz.tbsg.games.exceptions.EventException;
import ch.zxseitz.tbsg.server.games.GameProxy;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class GameSocketHandler extends TextWebSocketHandler {
    public static final int CLIENT_ID = 1000;
    public static final int CLIENT_CHALLENGE = 1010;
    public static final int CLIENT_CHALLENGE_ABORT = 1011;
    public static final int CLIENT_CHALLENGE_ACCEPT = 1012;
    public static final int CLIENT_CHALLENGE_DECLINE = 1013;

    public static final int SERVER_ERROR = 0;
    public static final int SERVER_ID = 1100;
    public static final int SERVER_CHALLENGE = 1110;
    public static final int SERVER_CHALLENGE_ABORT = 1111;
    public static final int SERVER_CHALLENGE_ACCEPT = 1112;
    public static final int SERVER_CHALLENGE_DECLINE = 1113;

    private final Logger logger;
    private final ObjectMapper mapper;
    private final GameProxy proxy;
    private final Map<String, Client> clients;

    public GameSocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.mapper = new ObjectMapper();
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
    private static <T, L> T safe(Callable<T> callable, IProtectable<L>... locks) throws Exception {
        var sorted = Arrays.stream(locks).sorted().collect(Collectors.toList());  //prevent deadlocks
        sorted.forEach(IProtectable::lock);
        try {
            return callable.call();
        } finally {
            sorted.forEach(IProtectable::unlock);
        }
    }

    /**
     * Handles client connection
     *
     * @param session client websocket session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Connected client: {}", session.getId());
        // todo: read auth infos
        var client = new Client(session);
        clients.put(session.getId(), client);
        try {
            client.send(createIdEvent(client));
        } catch (Exception ignore) {

        }
    }

    /**
     * Handles client disconnection
     *
     * @param session client websocket session
     * @param status close status
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Disconnected client: {}", session.getId());
        var client = clients.remove(session.getId());
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
            var node = mapper.readTree(message.getPayload());
            var codeNode = node.get("code");
            var argNode = node.get("args");
            if (codeNode == null || !codeNode.isInt()) {
                throw new EventException("Missing event code");
            }
            if (argNode == null || !argNode.isObject()) {
                throw new EventException("Missing arguments");
            }
            var eventCode = codeNode.intValue();

            // lobby client events
            switch (eventCode) {
                case CLIENT_CHALLENGE: {
                    var opponentId = readClientArgument(argNode, "opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    safe(() -> {
                        client.getChallenges().add(opponent);
                        opponent.send(createChallengeEvent(client));
                        return null;
                    }, client);
                    break;
                }
                case CLIENT_CHALLENGE_ABORT: {
                    var opponentId = readClientArgument(argNode, "opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    safe(() -> {
                        if (client.getChallenges().remove(opponent)) {
                            opponent.send(createChallengeAbortEvent(client));
                        } else {
                            throw new TbsgException(String.format("Opponent [%s] is not challenged by you", opponentId));
                        }
                        return null;
                    }, client);
                    break;
                }
                case CLIENT_CHALLENGE_ACCEPT: {
                    var opponentId = readClientArgument(argNode, "opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    safe(() -> {
                        // todo queuing accepts
                        if (client.getMatch() == null && opponent.getMatch() == null) {
                            opponent.send(createChallengeAcceptEvent(client));
                            var board = proxy.createBoard();
                            var match = new Match(new Protector<>(board), client, opponent);
                            client.setMatch(match);
                            opponent.setMatch(match);
                        } else {
                            throw new TbsgException(String.format("You or opponent [%s] is currently in game", opponentId));
                        }
                        return null;
                    }, client, opponent);
                    break;
                }
                case CLIENT_CHALLENGE_DECLINE: {
                    var opponentId = readClientArgument(argNode, "opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    safe(() -> {
                        if (opponent.getChallenges().remove(client)) {
                            opponent.send(createChallengeDeclineEvent(client));
                        } else {
                            throw new TbsgException(String.format("Opponent [%s] is not challenged by you", opponentId));
                        }
                        return null;
                    }, opponent);
                    break;
                }
                default: {
                    var event = proxy.getClientEvents().get(eventCode);
                    if (event != null) {
                        var args = readClientGameArguments(argNode, event.getArgs());
                        safe(() -> {
                            var match = client.getMatch();
                            if (match != null) {
                                var boardProtector = match.getBoard();
                                safe(() -> {
                                    var board = boardProtector.get();
                                    event.getMethod().invoke(proxy.getGame(), board, args);
                                    // todo update client states
                                    return null;
                                }, boardProtector);
                            } else {
                                throw new TbsgException("You are not in game to invoke this event");
                            }
                            return null;
                        }, client);
                    }
                }
            }
        } catch (Exception e) {
            try {
                client.send(e.getMessage());
            } catch (ClientException ce) {
                logger.warn(ce.getMessage(), ce);
            }
        }
    }

    private Object[] readClientGameArguments(JsonNode argsNode, ArgumentFormat[] argsFormat) throws EventException {
        var args = new Object[argsFormat.length];
        for (var i = 0; i < args.length; i++) {
            var argFormat = argsFormat[i];
            var argName = argFormat.getName();
            var argNode = argsNode.get(argName);
            if (argNode == null) {
                throw new EventException("Missing event argument " + argName);
            }
            try {
                args[i] = mapper.treeToValue(argNode, argFormat.getType());
            } catch (JsonProcessingException e) {
                throw new EventException("Invalid event argument " + argName);
            }
        }
        return args;
    }

    private <T> T readClientArgument(JsonNode node, String name, Class<T> type) throws  EventException {
        var argNode = node.get(name);
        if (argNode != null) {
            try {
                mapper.convertValue(argNode, type);
            } catch (IllegalArgumentException iae) {
                throw new EventException("Cannot convert argument " + name + " to " + type.getSimpleName());
            }
        }
        throw new EventException("Missing argument " + name);
    }

    public String createErrorEvent(String reason) throws JsonProcessingException {
        return stringify(SERVER_ERROR,
                Map.entry("reason", reason));
    }

    public String createIdEvent(Client sender) throws JsonProcessingException {
        return stringify(SERVER_ID,
                Map.entry("id", sender.getId()));
    }

    public String createChallengeEvent(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE,
                Map.entry("opponent", opponent.getId()));
    }

    public String createChallengeAbortEvent(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_ABORT,
                Map.entry("opponent", opponent.getId()));
    }

    private String createChallengeAcceptEvent(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_ACCEPT,
                Map.entry("opponent", opponent.getId()));
    }

    private String createChallengeDeclineEvent(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_DECLINE,
                Map.entry("opponent", opponent.getId()));
    }

    @SafeVarargs
    private String stringify(int eventCode, Map.Entry<String, Object> ...args) throws JsonProcessingException {
        var node = mapper.createObjectNode();
        node.put("code", eventCode);
        var argsNode = node.putObject("args");
        Arrays.stream(args).forEach(arg -> {
            argsNode.set(arg.getKey(), mapper.convertValue(arg.getValue(), JsonNode.class));
        });
        return mapper.writeValueAsString(node);
    }
}

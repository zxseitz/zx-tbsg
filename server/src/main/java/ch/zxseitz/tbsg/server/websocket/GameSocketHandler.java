package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.Color;
import ch.zxseitz.tbsg.games.GameState;
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
    public static final int CLIENT_UPDATE = 2000;

    public static final int SERVER_ERROR = 0;
    public static final int SERVER_ID = 1100;
    public static final int SERVER_CHALLENGE = 1110;
    public static final int SERVER_CHALLENGE_ABORT = 1111;
    public static final int SERVER_CHALLENGE_ACCEPT = 1112;
    public static final int SERVER_CHALLENGE_DECLINE = 1113;
    public static final int SERVER_GAME_INIT_NEXT = 2100;
    public static final int SERVER_GAME_INIT = 2101;
    public static final int SERVER_GAME_UPDATE_NEXT = 2110;
    public static final int SERVER_GAME_UPDATE = 2111;
    public static final int SERVER_GAME_END_VICTORY = 2120;
    public static final int SERVER_GAME_END_DEFEAT = 2121;
    public static final int SERVER_GAME_END_TIE = 2122;

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
            client.send(createIdMessage(client));
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
        // todo delete challenges and abort open matches
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
            var messageCode = codeNode.intValue();

            // lobby client events
            switch (messageCode) {
                case CLIENT_CHALLENGE: {
                    var opponentId = readClientArgument(argNode, "opponent", String.class);
                    var opponent = clients.get(opponentId);
                    if (opponent == null) {
                        throw new TbsgException(String.format("Opponent [%s] is not connected", opponentId));
                    }
                    safe(() -> {
                        client.getChallenges().add(opponent);
                        opponent.send(createChallengeMessage(client));
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
                            opponent.send(createChallengeAbortMessage(client));
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
                            opponent.send(createChallengeAcceptMessage(client));
                            var board = proxy.createGame();
                            var match = new Match(new Protector<>(board), client, opponent);
                            client.setMatch(match);
                            opponent.setMatch(match);

                            //todo server init message
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
                            opponent.send(createChallengeDeclineMessage(client));
                        } else {
                            throw new TbsgException(String.format("Opponent [%s] is not challenged by you", opponentId));
                        }
                        return null;
                    }, opponent);
                    break;
                }
                case CLIENT_UPDATE: {
                    var arguments = readClientGameArguments(argNode, proxy.getUpdateArguments());
                    safe(() ->  {
                        var match = client.getMatch();
                        var gameProtector = match.getGame();
                        var opponent = match.getOpponent(client);
                        safe(() ->  {
                            var game = gameProtector.get();
                            if (game.getState() == GameState.FINISHED) {
                                client.send(createErrorMessage("Game is already finished"));
                            }
                            if (!game.getNext().equals(match.getColor(client))) {
                                client.send(createErrorMessage("Not your turn"));
                            }
                            proxy.performUpdate(game, arguments);

                            //todo update game

                            return null;
                        }, gameProtector);
                        return null;
                    }, client);
                }
                default: {
                    client.send(createErrorMessage("unknown message code " + messageCode));
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            try {
                client.send(createErrorMessage(e.getMessage()));
            } catch (Exception ce) {
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

    public String createErrorMessage(String reason) throws JsonProcessingException {
        return stringify(SERVER_ERROR,
                Map.entry("reason", reason)
        );
    }

    public String createIdMessage(Client sender) throws JsonProcessingException {
        return stringify(SERVER_ID,
                Map.entry("id", sender.getId())
        );
    }

    public String createChallengeMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE,
                Map.entry("opponent", opponent.getId())
        );
    }

    public String createChallengeAbortMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_ABORT,
                Map.entry("opponent", opponent.getId())
        );
    }

    private String createChallengeAcceptMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_ACCEPT,
                Map.entry("opponent", opponent.getId())
        );
    }

    private String createChallengeDeclineMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_DECLINE,
                Map.entry("opponent", opponent.getId())
        );
    }

    private String createGameInitNextMessage(Color color, Object board, Object preview) throws JsonProcessingException {
        return stringify(SERVER_GAME_INIT_NEXT,
                Map.entry("color", color.ordinal()),
                Map.entry("board", board),
                Map.entry("preview", preview)
        );
    }

    private String createGameInitMessage(Color color, Object board) throws JsonProcessingException {
        return stringify(SERVER_GAME_INIT,
                Map.entry("color", color.ordinal()),
                Map.entry("board", board)
        );
    }

    private String createGameUpdateNextMessage(int source, Object board, Object preview) throws JsonProcessingException {
        return stringify(SERVER_GAME_UPDATE_NEXT,
                Map.entry("source", source),
                Map.entry("board", board),
                Map.entry("preview", preview)
        );
    }

    private String createGameUpdateMessage(int source, Object board) throws JsonProcessingException {
        return stringify(SERVER_GAME_UPDATE,
                Map.entry("source", source),
                Map.entry("board", board)
        );
    }

    private String createGameEndVictoryMessage(int source, Object board) throws JsonProcessingException {
        return stringify(SERVER_GAME_END_VICTORY,
                Map.entry("source", source),
                Map.entry("board", board)
        );
    }

    private String createGameEndDefeatMessage(int source, Object board) throws JsonProcessingException {
        return stringify(SERVER_GAME_END_DEFEAT,
                Map.entry("source", source),
                Map.entry("board", board)
        );
    }

    private String createGameEndTieMessage(int source, Object board) throws JsonProcessingException {
        return stringify(SERVER_GAME_END_TIE,
                Map.entry("source", source),
                Map.entry("board", board)
        );
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

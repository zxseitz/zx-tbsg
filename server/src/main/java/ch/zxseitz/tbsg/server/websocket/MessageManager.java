package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.exceptions.EventException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class MessageManager {
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

    private static final ObjectMapper mapper = new ObjectMapper();

    private MessageManager() {
        //singleton
    }

    public static Map.Entry<Integer, JsonNode> parseClientMessage(String message)
            throws JsonProcessingException, EventException {
        var node = mapper.readTree(message);
        var codeNode = node.get("code");
        var argNode = node.get("args");
        if (codeNode == null || !codeNode.isInt()) {
            throw new EventException("Missing event code");
        }
        if (argNode == null || !argNode.isObject()) {
            throw new EventException("Missing arguments");
        }
        var messageCode = codeNode.intValue();
        return Map.entry(messageCode, argNode);
    }

    public static Object readClientGameArguments(JsonNode argsNode, Class<?> actionClass)
            throws EventException {
        try {
            return mapper.treeToValue(argsNode, actionClass);
        } catch (JsonProcessingException e) {
            throw new EventException("Invalid arguments " + argsNode.toString());
        }
    }

    public static  <T> T readClientArgument(JsonNode node, String name, Class<T> type)
            throws  EventException {
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

    public static String createErrorMessage(String reason) throws JsonProcessingException {
        return stringify(SERVER_ERROR,
                Map.entry("reason", reason)
        );
    }

    public static String createIdMessage(Client sender) throws JsonProcessingException {
        return stringify(SERVER_ID,
                Map.entry("id", sender.getId())
        );
    }

    public static String createChallengeMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE,
                Map.entry("opponent", opponent.getId())
        );
    }

    public static String createChallengeAbortMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_ABORT,
                Map.entry("opponent", opponent.getId())
        );
    }

    public static String createChallengeAcceptMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_ACCEPT,
                Map.entry("opponent", opponent.getId())
        );
    }

    public static String createChallengeDeclineMessage(Client opponent) throws JsonProcessingException {
        return stringify(SERVER_CHALLENGE_DECLINE,
                Map.entry("opponent", opponent.getId())
        );
    }

    public static String createGameInitNextMessage(int color, int[] board, Collection<Object> preview)
            throws JsonProcessingException {
        return stringify(SERVER_GAME_INIT_NEXT,
                Map.entry("color", color),
                Map.entry("board", board),
                Map.entry("preview", preview)
        );
    }

    public static String createGameInitMessage(int color, int[] board) throws JsonProcessingException {
        return stringify(SERVER_GAME_INIT,
                Map.entry("color", color),
                Map.entry("board", board)
        );
    }

    public static String createGameUpdateNextMessage(Object source, int[] board, Collection<Object> preview)
            throws JsonProcessingException {
        return stringify(SERVER_GAME_UPDATE_NEXT,
                Map.entry("source", source),
                Map.entry("board", board),
                Map.entry("preview", preview)
        );
    }

    public static String createGameUpdateMessage(Object source, int[] board) throws JsonProcessingException {
        return stringify(SERVER_GAME_UPDATE,
                Map.entry("source", source),
                Map.entry("board", board)
        );
    }

    public static String createGameEndVictoryMessage(Object source, int[] board) throws JsonProcessingException {
        return stringify(SERVER_GAME_END_VICTORY,
                Map.entry("source", source),
                Map.entry("board", board)
        );
    }

    public static String createGameEndDefeatMessage(Object source, int[] board) throws JsonProcessingException {
        return stringify(SERVER_GAME_END_DEFEAT,
                Map.entry("source", source),
                Map.entry("board", board)
        );
    }

    public static String createGameEndTieMessage(Object source, int[] board) throws JsonProcessingException {
        return stringify(SERVER_GAME_END_TIE,
                Map.entry("source", source),
                Map.entry("board", board)
        );
    }

    @SafeVarargs
    private static String stringify(int eventCode, Map.Entry<String, Object> ...args)
            throws JsonProcessingException {
        var node = mapper.createObjectNode();
        node.put("code", eventCode);
        var argsNode = node.putObject("args");
        Arrays.stream(args).forEach(arg -> {
            argsNode.set(arg.getKey(), mapper.convertValue(arg.getValue(), JsonNode.class));
        });
        return mapper.writeValueAsString(node);
    }
}

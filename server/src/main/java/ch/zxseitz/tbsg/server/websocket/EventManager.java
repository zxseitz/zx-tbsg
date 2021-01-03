package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.Event;
import ch.zxseitz.tbsg.games.EventException;
import ch.zxseitz.tbsg.games.IEvent;

import ch.zxseitz.tbsg.games.SimpleEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class EventManager {
    public static final int CODE_ERROR = 0;
    public static final int CODE_CHALLENGE = 1001;
    public static final int CODE_CHALLENGE_ABORT = 1002;
    public static final int CODE_CHALLENGE_ACCEPT = 1003;
    public static final int CODE_CHALLENGE_DECLINE = 1004;

    private static final ObjectMapper mapper = new ObjectMapper();

    private EventManager() {
        // singleton
    }

    /**
     * Parses a json message into an event
     *
     * @param json message
     * @return event
     * @throws JsonProcessingException if the json message is malformed
     * @throws EventException if the json message does not contain an event code
     */
    public static IEvent parse(String json) throws JsonProcessingException, EventException {
        var node = mapper.readTree(json);
        var codeNode = node.get("code");
        if (codeNode == null || !codeNode.isInt()) {
            throw new EventException("Missing event code");
        }
        var argsNode = node.get("args");
        if (argsNode != null && argsNode.isObject()) {
            var event = new Event(codeNode.intValue());
            var it = argsNode.fields();
            while (it.hasNext()) {
                var arg = it.next();
                event.addArgument(arg.getKey(), mapper.convertValue(arg.getValue(), Object.class));
            }
            return event;
        }
        return new SimpleEvent(codeNode.intValue());
    }

    /**
     * Converts an event into a json message
     *
     * @param event event
     * @return json message
     * @throws JsonProcessingException if the event is malformed
     */
    public static String stringify(IEvent event) throws JsonProcessingException {
        var node = mapper.createObjectNode();
        node.put("code", event.getCode());
        var argsNode = node.putObject("args");
        event.foreachArgument((name, value) -> {
            argsNode.set(name, mapper.convertValue(value, JsonNode.class));
        });
        return mapper.writeValueAsString(node);
    }

    /**
     * Creates a new challenge event
     *
     * @param opponent opponent player
     * @return challenge event
     */
    public static IEvent createChallengeEvent(Client opponent) {
        var event = new Event(CODE_CHALLENGE);
        event.addArgument("opponent", opponent.getId());
        return event;
    }

    /**
     * Creates a new challenge abort event
     *
     * @param opponent opponent player
     * @return challenge abort event
     */
    public static IEvent createChallengeAbortEvent(Client opponent) {
        var event = new Event(CODE_CHALLENGE_ABORT);
        event.addArgument("opponent", opponent.getId());
        return event;
    }

    /**
     * Creates a new challenge decline event
     *
     * @param opponent opponent player
     * @return challenge decline event
     */
    public static IEvent createChallengeDeclineEvent(Client opponent) {
        var event = new Event(CODE_CHALLENGE_DECLINE);
        event.addArgument("opponent", opponent.getId());
        return event;
    }

    /**
     * Creates a new error event
     *
     * @param reason why the error occurred
     * @return error event
     */
    public static IEvent createErrorEvent(String reason) {
        var event = new Event(CODE_ERROR);
        event.addArgument("reason", reason);
        return event;
    }
}

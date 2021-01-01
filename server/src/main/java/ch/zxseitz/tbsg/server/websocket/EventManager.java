package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.EventException;
import ch.zxseitz.tbsg.games.IEvent;
import ch.zxseitz.tbsg.games.SimpleEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EventManager {
    public static final int CLIENT_CODE_CHALLENGE = 1001;

    public static final int SERVER_CODE_CHALLENGE = 2001;

    private final ObjectMapper mapper;

    public EventManager() {
        this.mapper = new ObjectMapper();
    }

    public IEvent parse(String json) throws EventException {
        try {
            var node = mapper.readTree(json);
            var codeNode = node.get("code");
            if (codeNode == null) {
                throw new EventException("Missing event code");
            }
            var event = new SimpleEvent(codeNode.intValue());
            var argsNode = node.get("args");
            if (argsNode != null) {
                var it = argsNode.fields();
                while (it.hasNext()) {
                    var arg = it.next();
                    event.addArgument(arg.getKey(), mapper.convertValue(arg.getValue(), Object.class));
                }
            }
            return event;
        } catch (JsonProcessingException e) {
            throw new EventException("Malformed json string: " + e.getMessage());
        }
    }

    public String stringify(IEvent event) throws EventException {
        try {
            var rootNode = mapper.createObjectNode();
            rootNode.put("code", event.getCode());
            var argsNode = rootNode.putObject("args");
            event.foreachArgument((name, value) -> {
                argsNode.set(name, mapper.convertValue(value, JsonNode.class));
            });
            return mapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new EventException("Malformed event");
        }
    }

    @SafeVarargs
    public final String stringify(int code, Map.Entry<String, Object>... args) throws EventException {
        try {
            var rootNode = mapper.createObjectNode();
            rootNode.put("code", code);
            var argsNode = rootNode.putObject("args");
            if (args != null) {
                for (var arg : args) {
                    argsNode.set(arg.getKey(), mapper.convertValue(arg.getValue(), JsonNode.class));
                }
            }
            return mapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new EventException("Malformed event");
        }
    }
}

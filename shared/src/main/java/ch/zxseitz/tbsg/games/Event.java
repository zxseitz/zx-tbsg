package ch.zxseitz.tbsg.games;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Event implements IEvent {
    private final int code;
    private final Map<String, Object> args;

    public Event(int code) {
        this.code = code;
        this.args = new HashMap<>();
    }

    @Override
    public int getCode() {
        return code;
    }

    public void addArgument(String name, Object value) {
        args.put(name, value);
    }

    @Override
    public <T> T getArgument(String name, Class<T> tClass) throws EventException {
        var arg = args.get(name);
        if (arg == null) {
            throw new EventException("Argument " + name + " is missing");
        }
        if (!tClass.isInstance(arg)) {
            throw new EventException("Argument " + name + " is not of type " + tClass.getName());
        }
        return tClass.cast(arg);
    }

    @Override
    public int argumentSize() {
        return args.size();
    }

    @Override
    public void foreachArgument(BiConsumer<String, Object> consumer) {
        args.forEach(consumer);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("(").append(code).append(" [");
        var it = args.entrySet().iterator();
        if (it.hasNext()) {
            var firstArg = it.next();
            sb.append(firstArg.getKey()).append(" ").append(firstArg.getValue());
        }
        while (it.hasNext()) {
            var arg = it.next();
            sb.append(" ,").append(arg.getKey()).append(" ").append(arg.getValue());
        }
        sb.append("])");
        return sb.toString();
    }
}

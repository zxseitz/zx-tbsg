package ch.zxseitz.tbsg.games;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SimpleEvent implements IEvent {
    private int code;
    private final Map<String, Object> args;

    public SimpleEvent(int code) {
        this.code = code;
        this.args = new HashMap<>();
    }

    @Override
    public int getCode() {
        return code;
    }

    public <T> void addArgument(String name, T value) {
        args.put(name, value);
    }

    @Override
    public <T> T getArgument(String name, Class<T> tClass) {
        var arg = args.get(name);
        if (tClass.isInstance(arg)) {
            return tClass.cast(arg);
        }
        return null;
    }

    @Override
    public int argumentSize() {
        return args.size();
    }

    @Override
    public void foreachArgument(BiConsumer<String, Object> consumer) {
        args.forEach(consumer);
    }
}

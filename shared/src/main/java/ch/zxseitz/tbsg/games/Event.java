package ch.zxseitz.tbsg.games;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Event implements IEvent {
    private final int code;
    private final Map<String, Object> args;

    @SafeVarargs
    public Event(int code, Map.Entry<String, Object>... args) {
        this.code = code;
        this.args = Arrays.stream(args)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public int getCode() {
        return code;
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
}

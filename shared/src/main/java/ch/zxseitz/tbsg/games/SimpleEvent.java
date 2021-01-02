package ch.zxseitz.tbsg.games;

import java.util.function.BiConsumer;

public class SimpleEvent implements IEvent {
    private final int code;

    public SimpleEvent(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public <T> T getArgument(String name, Class<T> tClass) throws EventException {
        throw new EventException("Argument " + name + " is missing");
    }

    @Override
    public int argumentSize() {
        return 0;
    }

    @Override
    public void foreachArgument(BiConsumer<String, Object> consumer) {
        //ignore
    }
}

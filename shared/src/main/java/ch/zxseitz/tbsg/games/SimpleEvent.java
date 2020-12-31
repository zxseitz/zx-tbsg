package ch.zxseitz.tbsg.games;

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
    public <T> T getArgument(Class<T> tClass, int pos) {
        return null;
    }
}

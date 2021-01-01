package ch.zxseitz.tbsg.games;

import java.util.function.BiConsumer;

public interface IEvent {
    int getCode();
    int argumentSize();
    <T> T getArgument(String name, Class<T> tClass);
    void foreachArgument(BiConsumer<String, Object> consumer);
}

package ch.zxseitz.tbsg.games;

public interface IEvent {
    int getCode();
    <T> T getArgument(Class<T> tClass, int pos);
}

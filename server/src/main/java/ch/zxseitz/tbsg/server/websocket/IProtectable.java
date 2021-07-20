package ch.zxseitz.tbsg.server.websocket;

public interface IProtectable<T> extends Comparable<T> {
    void lock();
    void unlock();
}

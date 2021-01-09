package ch.zxseitz.tbsg.server.websocket;

public interface ILockable<T> extends Comparable<T> {
    void lock();
    void unlock();
}

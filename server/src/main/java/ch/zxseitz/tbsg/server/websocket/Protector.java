package ch.zxseitz.tbsg.server.websocket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Protector<T extends Comparable<T>> implements IProtectable<T> {
    private final Lock lock;
    private final T t;

    public Protector(T t) {
        this.lock = new ReentrantLock();
        this.t = t;
    }

    public T get() {
        return t;
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public int compareTo(T o) {
        return t.compareTo(o);
    }
}

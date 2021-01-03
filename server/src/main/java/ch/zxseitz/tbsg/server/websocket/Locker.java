package ch.zxseitz.tbsg.server.websocket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Locker<T extends Comparable<T>> implements ILockable<T> {
    private final Lock lock;
    private final T t;

    public Locker(T t) {
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

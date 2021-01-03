package ch.zxseitz.tbsg.server.websocket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Locker<T extends Comparable<T>> implements Comparable<Locker<T>> {
    private final Lock lock;
    private final T t;

    public Locker(T t) {
        this.lock = new ReentrantLock();
        this.t = t;
    }

    public T get() {
        return t;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
    @Override
    public int compareTo(Locker<T> o) {
        return t.compareTo(o.t);
    }
}

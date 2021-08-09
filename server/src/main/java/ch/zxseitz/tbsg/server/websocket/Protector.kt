package ch.zxseitz.tbsg.server.websocket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Protector<T: Comparable<T>>(val t: T): IProtectable<T> {
    private val lock: Lock = ReentrantLock()

    override fun lock() {
        lock.lock();
    }

    override fun unlock() {
        lock.unlock();
    }

    override fun compareTo(other: T): Int {
        return t.compareTo(other);
    }
}

package ch.zxseitz.tbsg.server.websocket;

import java.util.concurrent.locks.ReentrantReadWriteLock;

class Lobby {
    private val readLock: ReentrantReadWriteLock.ReadLock
    private val writeLock: ReentrantReadWriteLock.WriteLock
    private val clients: MutableMap<String, Client> = HashMap()

    init {
        val lock = ReentrantReadWriteLock(true)
        readLock = lock.readLock()
        writeLock = lock.writeLock()
    }

    operator fun get(id: String): Client? {
        readLock.lock()
        try {
            return clients[id]
        } finally {
            readLock.unlock()
        }
    }

    val size: Int
        get() {
            readLock.lock()
            try {
                return clients.size
            } finally {
                readLock.unlock()
            }
        }

    fun add(client: Client): Boolean {
        writeLock.lock()
        try {
            if (!clients.containsKey(client.id)) {
                clients[client.id] = client
                return true
            }
            return false
        } finally {
            writeLock.unlock()
        }
    }

    fun remove(id: String): Client? {
        writeLock.lock()
        try {
            return clients.remove(id)
        } finally {
            writeLock.unlock()
        }
    }

    fun clear() {
        writeLock.lock()
        try {
            clients.clear()
        } finally {
            writeLock.unlock()
        }
    }

}

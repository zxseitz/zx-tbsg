package ch.zxseitz.tbsg.server.websocket

import ch.zxseitz.tbsg.games.*
import ch.zxseitz.tbsg.games.exceptions.ClientException
import ch.zxseitz.tbsg.server.model.User
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.io.IOException

class Client(private val session: WebSocketSession, private val user: User? = null): IProtectable<Client> {
    private val lock: Lock = ReentrantLock()
    val challenges: MutableSet<Client> = HashSet()
    var match: Match? = null

    val id: String
        get() = session.id

    val player: IPlayer?
        get() = user

    fun send(message: String) {
        try {
            session.sendMessage(TextMessage(message))
        } catch (e: IOException) {
            throw ClientException("Cannot send message $message to client $this", e)
        }
    }

   override fun lock() {
        lock.lock()
    }

    override fun unlock() {
        lock.unlock()
    }

    override fun compareTo(other: Client): Int {
        return id.compareTo(other.id)
    }

    override fun toString(): String {
        return "${session.id}, ${user?.username ?: "guest"}"
    }
}

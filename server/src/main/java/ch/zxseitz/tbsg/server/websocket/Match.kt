package ch.zxseitz.tbsg.server.websocket

import ch.zxseitz.tbsg.games.IGame

// 1v1 match
class Match(val game: Protector<IGame<Any>>, private val clients: Map<Int, Client>) {
    fun getColor(client: Client): Int {
        return clients.entries.stream()
                .filter{entry -> entry.value == client }
                .mapToInt{entry -> entry.key}
                .findFirst().orElse(0)
    }

    fun getOpponent(client: Client): Client {
        return clients.values.stream()
                .filter{client1 -> !client1.equals(client)}
                .findFirst().orElse(null)
    }
}

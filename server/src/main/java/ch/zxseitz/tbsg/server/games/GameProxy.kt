package ch.zxseitz.tbsg.server.games

import ch.zxseitz.tbsg.TbsgException
import ch.zxseitz.tbsg.games.IGame

data class GameProxy(
    val name: String,
    val colors: Map<Int, String>,
    val gameClass: Class<IGame<Any>>,
    val actionClass: Class<*>) {

    fun createGame(): IGame<Any> {
        try {
            return gameClass.getConstructor().newInstance()
        } catch (e: Exception) {
            throw TbsgException("Unable to create $name game instance", e)
        }
    }
}

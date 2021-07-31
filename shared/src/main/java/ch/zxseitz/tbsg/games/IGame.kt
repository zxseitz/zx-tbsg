package ch.zxseitz.tbsg.games;

import ch.zxseitz.tbsg.games.exceptions.GameException;
import kotlin.jvm.Throws

interface IGame<T>: Comparable<IGame<*>> {
    val id: String
    val next: Int
    val state: GameState
    val board: IntArray
    val preview: Collection<T>
    fun init();
    @Throws(GameException::class)
    fun update(action: T)
}

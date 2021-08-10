package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*
import ch.zxseitz.tbsg.games.annotations.TbsgGame
import ch.zxseitz.tbsg.games.annotations.Color
import ch.zxseitz.tbsg.games.exceptions.GameException

import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@TbsgGame(name = "reversi", colors = [
    Color(value = 1, name = "black"),
    Color(value = 2, name = "white"),
])
class Reversi(private val _board: Board = Board(),
              private val _actions: MutableMap<Action, Collection<Int>> = HashMap()): IGame<Action> {
    companion object {
        @Synchronized fun generateId(): String {
            return UUID.randomUUID().toString()
        }
    }

    override val id: String = generateId()

    override val board: IntArray
        get() = _board.fields

    override val preview: MutableCollection<Action>
        get() = HashSet(_actions.keys)

    internal var _next: Int = 0 //accessible for testing
    override val next: Int
        get() = _next

    internal var _state: GameState = GameState.CREATED //accessible for testing
    override val state: GameState
        get() = _state

    override fun compareTo(other: IGame<*>): Int {
        return id.compareTo(other.id)
    }

    override fun init() {
        _board.init()
        _actions[Action(19)] = Collections.singletonList(27)
        _actions[Action(26)] = Collections.singletonList(27)
        _actions[Action(37)] = Collections.singletonList(36)
        _actions[Action(44)] = Collections.singletonList(36)
        _next = 1
        _state = GameState.RUNNING
    }

    override fun update(action: Action) {
        val collection = _actions[action] ?: throw GameException("Index defined in action is invalid")
        _board[action.index] = _next
        _board[collection] = _next
        val opponentColor = 3 - _next

        // update state and actions
        _actions.clear()
        val emptyFields = TreeSet<Int>()
        var blackCount = 0
        var whiteCount = 0
        for (i in 0 until Board.SIZE) {
            when (board[i]) {
                1 -> blackCount++
                2 -> whiteCount++
                else -> emptyFields.add(i)
            }
        }

        // check next opponent turn
        addActions(emptyFields, opponentColor, _next)
        if (_actions.size > 0) { // Map::isNotEmpty inline function cannot be mocked by MockK, Aug. 2021
            _next = opponentColor
            return
        }

        // check next own turn, if opponent has no legal moves
        addActions(emptyFields, _next, opponentColor)
        if (_actions.size > 0) return // Map::isNotEmpty inline function cannot be mocked by MockK, Aug. 2021

        // check end state, if no one has legal moves
        _state = GameState.FINISHED
        _next = when {
            blackCount > whiteCount -> 1
            blackCount < whiteCount -> 2
            else -> 0
        }
    }

    private fun addActions(emptyFields: Set<Int>, color: Int, opponentColor: Int) {
        for (ai in emptyFields) {
            val actions = _board.getOpponentTokens(ai, color, opponentColor)
            if (actions.size > 0) { // Set::isNotEmpty inline function cannot be mocked by MockK, Aug. 2021
                _actions[Action(ai)] = actions
            }
        }
    }
}

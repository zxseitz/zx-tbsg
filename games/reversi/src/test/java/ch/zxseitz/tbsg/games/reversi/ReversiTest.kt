package ch.zxseitz.tbsg.games.reversi

import ch.zxseitz.tbsg.games.*
import ch.zxseitz.tbsg.games.exceptions.GameException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.mockito.Mockito.*

@RunWith(PowerMockRunner::class)
@PrepareForTest(Reversi::class, Board::class, Action::class)
class ReversiTest {
    private val board = mock(Board::class.java)
    private val actions: MutableMap<Action, Collection<Int>> =
        mock(MutableMap::class.java) as MutableMap<Action, Collection<Int>>

    @BeforeEach
    fun setUp() {
        reset(board, actions)
    }

    @Test
    fun testUpdateInvalidIndex() {
        try {
            val action = mock(Action::class.java)
            doReturn(null).`when`(actions)[action]
            val match = Reversi(board, actions)
            match.update(action)
            fail()
        } catch (ge: GameException) {
            assertEquals("Index defined in action is invalid", ge.message)
        }
    }

    @Test
    fun testUpdatePlaceBlackNextWhite() {
        val indexBlack = 39
        val indexNextWhite = 58
        val actionBlack = mock(Action::class.java)
        val actionWhite = mock(Action::class.java)
        val actionsBlack = setOf(38, 37, 36)
        val actionsNextWhite = setOf(50, 42, 34)
        val fields = intArrayOf(
            0, 0, 2, 2, 2, 2, 0, 0,
            0, 0, 2, 2, 2, 1, 0, 0,
            1, 0, 1, 1, 2, 2, 2, 2,
            1, 2, 2, 1, 2, 1, 2, 2,
            1, 1, 1, 1, 2, 2, 2, 0,
            1, 2, 1, 1, 2, 2, 0, 1,
            0, 2, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 0
        )

        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
        doReturn(indexBlack).`when`(actionBlack).index
        doReturn(fields).`when`(board).fields
        doReturn(actionsNextWhite).`when`(board).getOpponentTokens(indexNextWhite, 2, 1)
        doReturn(actionsBlack).`when`(actions)[actionBlack]
        doReturn(1).`when`(actions).size
        PowerMockito.whenNew(Action::class.java).withArguments(eq(indexNextWhite)).thenReturn(actionWhite)

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(board, times(1))[indexBlack] = 1
        verify(board, times(1))[actionsBlack] = 1

        // verify new white actions
        verify(actions, times(1))[eq(actionWhite)] = eq(actionsNextWhite)

        assertEquals(GameState.RUNNING, match.state)
        assertEquals(2, match.next)
    }

    @Test
    fun testInvokePlaceBlackNextBlack() {
        val indexBlack = 39
        val indexNextBlack = 58
        val blackAction = mock(Action::class.java)
        val blackNextAction = mock(Action::class.java)
        val actionsBlack = setOf(38, 37, 36)
        val actionsNextBlack = setOf(50, 42, 34)
        val fields = intArrayOf(
            0, 0, 2, 2, 2, 2, 0, 0,
            0, 0, 2, 2, 2, 1, 0, 0,
            1, 0, 1, 1, 2, 2, 2, 2,
            1, 2, 2, 1, 2, 1, 2, 2,
            1, 1, 1, 1, 2, 2, 2, 0,
            1, 2, 1, 1, 2, 2, 0, 1,
            0, 2, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 0
        )

        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
        doReturn(indexBlack).`when`(blackAction).index
        doReturn(indexNextBlack).`when`(blackNextAction).index
        doReturn(fields).`when`(board).fields
        doReturn(actionsNextBlack).`when`(board).getOpponentTokens(indexNextBlack, 2, 1)
        doReturn(0).doReturn(1).`when`(actions).size
        doReturn(actionsBlack).`when`(actions)[blackAction]
        PowerMockito.whenNew(Action::class.java).withArguments(eq(indexNextBlack)).thenReturn(blackNextAction)

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(blackAction)

        // verify black board changes
        verify(board, times(1))[indexBlack] = 1
        verify(board, times(1))[actionsBlack] = 1

        // verify new black actions
        verify(actions, times(1))[eq(blackNextAction)] = eq(actionsNextBlack)

        assertEquals(GameState.RUNNING, match.state)
        assertEquals(1, match.next)
    }

    @Test
    fun testInvokePlaceBlackWon() {
        val indexBlack = 39
        val actionBlack = mock(Action::class.java)
        val actionsBlack = setOf(38, 37, 36)
        val fields = intArrayOf(
            0, 0, 2, 2, 2, 2, 0, 0,
            0, 0, 2, 2, 2, 1, 0, 0,
            1, 0, 1, 1, 2, 2, 2, 0,
            1, 1, 1, 1, 2, 2, 2, 2,
            1, 1, 1, 1, 1, 1, 2, 0,
            1, 2, 1, 1, 1, 1, 0, 0,
            0, 2, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 0
        )

        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
        doReturn(fields).`when`(board).fields
        doReturn(indexBlack).`when`(actionBlack).index
        doReturn(actionsBlack).`when`(actions)[actionBlack]
        doReturn(0).`when`(actions).size

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(board, times(1))[indexBlack] = 1
        verify(board, times(1))[actionsBlack] = 1

        verify(actions, never())[any()] = anyCollection()

        assertEquals(GameState.FINISHED, match.state)
        assertEquals(1, match.next)
    }

    @Test
    fun testInvokePlaceWhiteWon() {
        val indexBlack = 39
        val actionBlack = mock(Action::class.java)
        val actionsBlack = setOf(38, 37, 36)
        val fields = intArrayOf(
            0, 0, 2, 2, 2, 2, 0, 0,
            0, 0, 2, 2, 2, 1, 0, 0,
            1, 0, 2, 2, 2, 2, 2, 0,
            1, 2, 2, 1, 2, 2, 2, 2,
            1, 1, 1, 1, 2, 2, 2, 0,
            1, 2, 2, 2, 2, 2, 0, 0,
            0, 2, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 0, 0
        )

        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
        doReturn(fields).`when`(board).fields
        doReturn(indexBlack).`when`(actionBlack).index
        doReturn(actionsBlack).`when`(actions)[actionBlack]
        doReturn(0).`when`(actions).size

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(board, times(1))[indexBlack] = 1
        verify(board, times(1))[actionsBlack] = 1

        verify(actions, never())[any()] = anyCollection()

        assertEquals(GameState.FINISHED, match.state)
        assertEquals(2, match.next)
    }

    @Test
    fun testInvokePlaceTie() {
        val indexBlack = 39
        val actionBlack = mock(Action::class.java)
        val actionsBlack = setOf(38, 37, 36)
        val fields = intArrayOf(
            0, 0, 2, 2, 2, 2, 0, 0,
            0, 0, 2, 2, 2, 1, 0, 0,
            1, 0, 1, 1, 2, 2, 2, 0,
            1, 2, 2, 1, 2, 2, 2, 2,
            1, 1, 1, 1, 1, 1, 2, 0,
            1, 2, 1, 1, 2, 2, 0, 0,
            0, 2, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 0, 0
        )

        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
        doReturn(fields).`when`(board).fields
        doReturn(indexBlack).`when`(actionBlack).index
        doReturn(actionsBlack).`when`(actions)[actionBlack]
        doReturn(0).`when`(actions).size

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(board, times(1))[indexBlack] = 1
        verify(board, times(1))[actionsBlack] = 1

        verify(actions, never())[any()] = anyCollection()

        assertEquals(GameState.FINISHED, match.state)
        assertEquals(0, match.next)
    }
}

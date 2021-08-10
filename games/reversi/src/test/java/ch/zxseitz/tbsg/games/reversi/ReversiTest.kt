package ch.zxseitz.tbsg.games.reversi

import ch.zxseitz.tbsg.games.*
import ch.zxseitz.tbsg.games.exceptions.GameException
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ReversiTest {
    @MockK(relaxed = true)
    private lateinit var board: Board
    @MockK(relaxed = true)
    private lateinit var actions: MutableMap<Action, Collection<Int>>

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun testUpdateInvalidIndex() {
        try {
            val action = mockkClass(Action::class)
            every { actions[action] } returns null
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
        val actionBlack = mockkClass(Action::class)
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
        val actionWhiteSlot = slot<Action>()
        val actionsNextWhiteSlot = slot<Collection<Int>>()
        fields.forEach { i -> every { board[i] } returns fields[i] }
        every { actionBlack.index } returns indexBlack
        every { board.fields } returns fields
        every { board.getOpponentTokens(any(), any(), any()) } returns setOf()
        every { board.getOpponentTokens(indexNextWhite, 2, 1) } returns actionsNextWhite
        every { actions[actionBlack] } returns actionsBlack
        every { actions[capture(actionWhiteSlot)] = capture(actionsNextWhiteSlot) } answers {}
        every { actions.size } returns 1

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(exactly = 1) {
            board[indexBlack] = 1
            board[actionsBlack] = 1
        }
        // verify new white actions
        assertEquals(indexNextWhite, actionWhiteSlot.captured.index)
        assertEquals(actionsNextWhite, actionsNextWhiteSlot.captured)
        assertEquals(GameState.RUNNING, match.state)
        assertEquals(2, match.next)
    }

//    @Test
//    fun testInvokePlaceBlackNextBlack() {
//        val indexBlack = 39
//        val indexNextBlack = 58
//        val blackAction = mockkClass(Action::class)
//        val blackNextAction = mockkClass(Action::class)
//        val actionsBlack = setOf(38, 37, 36)
//        val actionsNextBlack = setOf(50, 42, 34)
//        val fields = intArrayOf(
//            0, 0, 2, 2, 2, 2, 0, 0,
//            0, 0, 2, 2, 2, 1, 0, 0,
//            1, 0, 1, 1, 2, 2, 2, 2,
//            1, 2, 2, 1, 2, 1, 2, 2,
//            1, 1, 1, 1, 2, 2, 2, 0,
//            1, 2, 1, 1, 2, 2, 0, 1,
//            0, 2, 1, 1, 1, 0, 0, 0,
//            0, 0, 0, 1, 1, 1, 1, 0
//        )
//
//        fields.forEach { i -> every { board[i] } returns fields[i] }
//        every { blackAction.index } returns indexBlack
//        doReturn(indexNextBlack).`when`(blackNextAction).index
//        every { board.fields }
//        doReturn(fields).`when`(board).fields
//        doReturn(actionsNextBlack).`when`(board).getOpponentTokens(indexNextBlack, 2, 1)
//        doReturn(0).doReturn(1).`when`(actions).size
//        doReturn(actionsBlack).`when`(actions)[blackAction]
//        PowerMockito.whenNew(Action::class.java).withArguments(eq(indexNextBlack)).thenReturn(blackNextAction)
//
//        val match = Reversi(board, actions)
//        match._state = GameState.RUNNING
//        match._next = 1
//        match.update(blackAction)
//
//        // verify black board changes
//        verify(board, times(1))[indexBlack] = 1
//        verify(board, times(1))[actionsBlack] = 1
//
//        // verify new black actions
//        verify(actions, times(1))[eq(blackNextAction)] = eq(actionsNextBlack)
//
//        assertEquals(GameState.RUNNING, match.state)
//        assertEquals(1, match.next)
//    }
//
//    @Test
//    fun testInvokePlaceBlackWon() {
//        val indexBlack = 39
//        val actionBlack = mockkClass(Action::class)
//        val actionsBlack = setOf(38, 37, 36)
//        val fields = intArrayOf(
//            0, 0, 2, 2, 2, 2, 0, 0,
//            0, 0, 2, 2, 2, 1, 0, 0,
//            1, 0, 1, 1, 2, 2, 2, 0,
//            1, 1, 1, 1, 2, 2, 2, 2,
//            1, 1, 1, 1, 1, 1, 2, 0,
//            1, 2, 1, 1, 1, 1, 0, 0,
//            0, 2, 1, 1, 1, 0, 0, 0,
//            0, 0, 0, 1, 1, 1, 1, 0
//        )
//
//        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
//        doReturn(fields).`when`(board).fields
//        doReturn(indexBlack).`when`(actionBlack).index
//        doReturn(actionsBlack).`when`(actions)[actionBlack]
//        doReturn(0).`when`(actions).size
//
//        val match = Reversi(board, actions)
//        match._state = GameState.RUNNING
//        match._next = 1
//        match.update(actionBlack)
//
//        // verify black board changes
//        verify(board, times(1))[indexBlack] = 1
//        verify(board, times(1))[actionsBlack] = 1
//
//        verify(actions, never())[any()] = anyCollection()
//
//        assertEquals(GameState.FINISHED, match.state)
//        assertEquals(1, match.next)
//    }
//
//    @Test
//    fun testInvokePlaceWhiteWon() {
//        val indexBlack = 39
//        val actionBlack = mockkClass(Action::class)
//        val actionsBlack = setOf(38, 37, 36)
//        val fields = intArrayOf(
//            0, 0, 2, 2, 2, 2, 0, 0,
//            0, 0, 2, 2, 2, 1, 0, 0,
//            1, 0, 2, 2, 2, 2, 2, 0,
//            1, 2, 2, 1, 2, 2, 2, 2,
//            1, 1, 1, 1, 2, 2, 2, 0,
//            1, 2, 2, 2, 2, 2, 0, 0,
//            0, 2, 1, 1, 1, 0, 0, 0,
//            0, 0, 0, 1, 1, 1, 0, 0
//        )
//
//        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
//        doReturn(fields).`when`(board).fields
//        doReturn(indexBlack).`when`(actionBlack).index
//        doReturn(actionsBlack).`when`(actions)[actionBlack]
//        doReturn(0).`when`(actions).size
//
//        val match = Reversi(board, actions)
//        match._state = GameState.RUNNING
//        match._next = 1
//        match.update(actionBlack)
//
//        // verify black board changes
//        verify(board, times(1))[indexBlack] = 1
//        verify(board, times(1))[actionsBlack] = 1
//
//        verify(actions, never())[any()] = anyCollection()
//
//        assertEquals(GameState.FINISHED, match.state)
//        assertEquals(2, match.next)
//    }
//
//    @Test
//    fun testInvokePlaceTie() {
//        val indexBlack = 39
//        val actionBlack = mockkClass(Action::class)
//        val actionsBlack = setOf(38, 37, 36)
//        val fields = intArrayOf(
//            0, 0, 2, 2, 2, 2, 0, 0,
//            0, 0, 2, 2, 2, 1, 0, 0,
//            1, 0, 1, 1, 2, 2, 2, 0,
//            1, 2, 2, 1, 2, 2, 2, 2,
//            1, 1, 1, 1, 1, 1, 2, 0,
//            1, 2, 1, 1, 2, 2, 0, 0,
//            0, 2, 1, 1, 1, 0, 0, 0,
//            0, 0, 0, 1, 1, 1, 0, 0
//        )
//
//        fields.forEach { i -> doReturn(fields[i]).`when`(board)[i] }
//        doReturn(fields).`when`(board).fields
//        doReturn(indexBlack).`when`(actionBlack).index
//        doReturn(actionsBlack).`when`(actions)[actionBlack]
//        doReturn(0).`when`(actions).size
//
//        val match = Reversi(board, actions)
//        match._state = GameState.RUNNING
//        match._next = 1
//        match.update(actionBlack)
//
//        // verify black board changes
//        verify(board, times(1))[indexBlack] = 1
//        verify(board, times(1))[actionsBlack] = 1
//
//        verify(actions, never())[any()] = anyCollection()
//
//        assertEquals(GameState.FINISHED, match.state)
//        assertEquals(0, match.next)
//    }
}

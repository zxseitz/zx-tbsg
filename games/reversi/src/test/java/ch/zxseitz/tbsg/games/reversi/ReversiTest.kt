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
        every { board.getOpponentTokens(indexNextWhite, 2, 1) } returns actionsNextWhite
        every { actions[actionBlack] } returns actionsBlack
        every { actions[capture(actionWhiteSlot)] = capture(actionsNextWhiteSlot) } answers {}
        every { actions.size } returns 1 andThen 0

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
        // check next game state
        assertEquals(GameState.RUNNING, match.state)
        assertEquals(2, match.next)
    }

    @Test
    fun testInvokePlaceBlackNextBlack() {
        val indexBlack = 39
        val indexNextBlack = 58
        val blackAction = mockkClass(Action::class)
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
        val actionBlackSlot = slot<Action>()
        val actionsNextBlackSlot = slot<Collection<Int>>()

        fields.forEach { i -> every { board[i] } returns fields[i] }
        every { blackAction.index } returns indexBlack
        every { board.fields } returns fields
        every { board.getOpponentTokens(indexNextBlack, 2, 1) } returns actionsNextBlack
        every { actions.size } returns 0 andThen 1
        every { actions[blackAction] } returns actionsBlack
        every { actions[capture(actionBlackSlot)] = capture(actionsNextBlackSlot) } answers {}

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(blackAction)

        // verify black board changes
        verify(exactly = 1) {
            board[indexBlack] = 1
            board[actionsBlack] = 1
        }
        // verify new black actions
        assertEquals(indexNextBlack, actionBlackSlot.captured.index)
        assertEquals(actionsNextBlack, actionsNextBlackSlot.captured)
        // check next game state
        assertEquals(GameState.RUNNING, match.state)
        assertEquals(1, match.next)
    }

    @Test
    fun testInvokePlaceBlackWon() {
        val indexBlack = 39
        val actionBlack = mockkClass(Action::class)
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

        fields.forEach { i -> every { board[i] } returns fields[i] }
        every { board.fields } returns fields
        every { actionBlack.index } returns indexBlack
        every { actions[actionBlack] } returns actionsBlack
        every { actions.size } returns 0

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(exactly = 1) {
            board[indexBlack] = 1
            board[actionsBlack] = 1
        }
        verify(exactly = 0) { actions[any()] = any() }

        assertEquals(GameState.FINISHED, match.state)
        assertEquals(1, match.next)
    }

    @Test
    fun testInvokePlaceWhiteWon() {
        val indexBlack = 39
        val actionBlack = mockkClass(Action::class)
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

        fields.forEach { i -> every { board[i] } returns fields[i] }
        every { board.fields } returns fields
        every { actionBlack.index } returns indexBlack
        every { actions[actionBlack] } returns actionsBlack
        every { actions.size } returns 0

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(exactly = 1) {
            board[indexBlack] = 1
            board[actionsBlack] = 1
        }
        verify(exactly = 0) { actions[any()] = any() }

        assertEquals(GameState.FINISHED, match.state)
        assertEquals(2, match.next)
    }

    @Test
    fun testInvokePlaceTie() {
        val indexBlack = 39
        val actionBlack = mockkClass(Action::class)
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

        fields.forEach { i -> every { board[i] } returns fields[i] }
        every { board.fields } returns fields
        every { actionBlack.index } returns indexBlack
        every { actions[actionBlack] } returns actionsBlack
        every { actions.size } returns 0

        val match = Reversi(board, actions)
        match._state = GameState.RUNNING
        match._next = 1
        match.update(actionBlack)

        // verify black board changes
        verify(exactly = 1) {
            board[indexBlack] = 1
            board[actionsBlack] = 1
        }
        verify(exactly = 0) { actions[any()] = any() }

        assertEquals(GameState.FINISHED, match.state)
        assertEquals(0, match.next)
    }
}

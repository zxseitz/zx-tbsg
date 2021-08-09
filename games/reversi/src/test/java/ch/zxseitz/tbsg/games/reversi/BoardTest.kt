package ch.zxseitz.tbsg.games.reversi

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BoardTest {
    @Test
    fun testGetIndex() {
        assertEquals(22, Board.index(6, 2))
    }

    @Test
    fun testGetByIndex() {
        val board = Board(intArrayOf(
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 1, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 2, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
        ))
        assertEquals(-1, board[-1])
        assertEquals(0, board[0])
        assertEquals(1, board[18])
        assertEquals(2, board[45])
        assertEquals(0, board[63])
        assertEquals(-1, board[64])
    }

    @Test
    fun testSetByIndices() {
        val board = Board()
        board[setOf(34, 47, 63)] = 0
    }

    @Test
    fun testGetOpponentTokens() {
        val board = Board(intArrayOf(
                0, 0, 0, 2, 0, 0, 1, 0,
                0, 1, 0, 2, 0, 1, 0, 0,
                0, 0, 1, 2, 0, 0, 0, 0,
                1, 1, 1, 0, 0, 1, 2, 0,
                0, 0, 2, 1, 1, 0, 0, 0,
                0, 1, 0, 1, 0, 1, 0, 0,
                0, 0, 0, 1, 0, 0, 2, 0,
                0, 0, 0, 2, 0, 0, 0, 0
        ))
        assertTrue(board.getOpponentTokens(27, 2, 1)
                .containsAll(mutableSetOf(36, 45, 35, 43, 51)))
    }
}

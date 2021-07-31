package ch.zxseitz.tbsg.games.reversi

import org.junit.Assert
import org.junit.Test

class BoardTest {
    @Test
    fun testGetIndex() {
        Assert.assertEquals(22, Board.index(6, 2))
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
        Assert.assertEquals(-1, board[-1])
        Assert.assertEquals(0, board[0])
        Assert.assertEquals(1, board[18])
        Assert.assertEquals(2, board[45])
        Assert.assertEquals(0, board[63])
        Assert.assertEquals(-1, board[64])
    }

    @Test
    fun testSetByIndices() {
        val board = Board()
        board[setOf(34, 47, 63)] = 0
    }

    @Test
    fun testSetByIndicesFail() {
        try {
            val board = Board()
            board[setOf(34, 47, 65)] = 0
            Assert.fail()
        } catch (ignored: ArrayIndexOutOfBoundsException) {

        }
    }

    @Test
    fun testGetOpponentTokens() {
        val board = Board(intArrayOf(
                0, 0, 0, 2, 0, 0, 1, 0,
                0, 1, 0, 2, 0, 1, 0, 0,
                0, 0, 1, 2, 0, 0, 0, 0,
                1, 1, 1, 0, 0, 1, 2, 0, //
                0, 0, 2, 1, 1, 0, 0, 0,
                0, 1, 0, 1, 0, 1, 0, 0,
                0, 0, 0, 1, 0, 0, 2, 0,
                0, 0, 0, 2, 0, 0, 0, 0,
                //
        ))
        Assert.assertTrue(board.getOpponentTokens(27, 2, 1)
                .containsAll(mutableSetOf(36, 45, 35, 43, 51)))
    }
}

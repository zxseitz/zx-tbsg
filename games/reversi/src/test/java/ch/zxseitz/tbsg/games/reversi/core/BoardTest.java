package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.reversi.Reversi;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class BoardTest {
    @Test
    public void testGetIndex() {
        Assert.assertEquals(22, Board.getIndex(6, 2));
    }

    @Test
    public void testCovers() {
        Assert.assertTrue(Board.covers(3, 3));
        Assert.assertTrue(Board.covers(0, 0));
        Assert.assertTrue(Board.covers(7, 0));
        Assert.assertTrue(Board.covers(0, 7));
        Assert.assertTrue(Board.covers(7, 7));
        Assert.assertFalse(Board.covers(-1, 0));
        Assert.assertFalse(Board.covers(0, -1));
        Assert.assertFalse(Board.covers(-1, -1));
        Assert.assertFalse(Board.covers(8, 7));
        Assert.assertFalse(Board.covers(7, 8));
        Assert.assertFalse(Board.covers(8, 8));
    }

    @Test
    public void testGetByIndex() {
        var board = new Board(new int[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 1, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 2, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
        });
        Assert.assertEquals(Reversi.TOKEN_UNDEFINED, board.get(-1));
        Assert.assertEquals(Reversi.TOKEN_EMPTY, board.get(0));
        Assert.assertEquals(Reversi.TOKEN_BLACK, board.get(18));
        Assert.assertEquals(Reversi.TOKEN_WHITE, board.get(45));
        Assert.assertEquals(Reversi.TOKEN_EMPTY, board.get(63));
        Assert.assertEquals(Reversi.TOKEN_UNDEFINED, board.get(64));
    }

    @Test
    public void testGetByXY() {
        var board = new Board(new int[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 1, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 2, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
        });
        Assert.assertEquals(Reversi.TOKEN_UNDEFINED, board.get(-1, 0));
        Assert.assertEquals(Reversi.TOKEN_UNDEFINED, board.get(0, -1));
        Assert.assertEquals(Reversi.TOKEN_EMPTY, board.get(0, 0));
        Assert.assertEquals(Reversi.TOKEN_BLACK, board.get(2, 2));
        Assert.assertEquals(Reversi.TOKEN_WHITE, board.get(5, 5));
        Assert.assertEquals(Reversi.TOKEN_EMPTY, board.get(7, 7));
        Assert.assertEquals(Reversi.TOKEN_UNDEFINED, board.get(8, 0));
        Assert.assertEquals(Reversi.TOKEN_UNDEFINED, board.get(0, 8));
    }

    @Test
    public void testSetByIndex() {
        var board = new Board();
        Assert.assertFalse(board.set(-1, Reversi.TOKEN_EMPTY));
        Assert.assertTrue(board.set(0, Reversi.TOKEN_EMPTY));
        Assert.assertTrue(board.set(63, Reversi.TOKEN_EMPTY));
        Assert.assertFalse(board.set(64, Reversi.TOKEN_EMPTY));
    }

    @Test
    public void testSetByIndices() throws ArrayIndexOutOfBoundsException {
        var board = new Board();
        board.set(Set.of(34, 47, 63), Reversi.TOKEN_EMPTY);
    }

    @Test
    public void testSetByIndicesFail() {
        try {
            var board = new Board();
            board.set(Set.of(34, 47, 65), Reversi.TOKEN_EMPTY);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }
    }

    @Test
    public void testGetOpponentTokens() {
        var board = new Board(new int[] {
                0, 0, 0, 2, 0, 0, 1, 0,
                0, 1, 0, 2, 0, 1, 0, 0,
                0, 0, 1, 2, 0, 0, 0, 0,
                1, 1, 1, 0, 0, 1, 2, 0, //
                0, 0, 2, 1, 1, 0, 0, 0,
                0, 1, 0, 1, 0, 1, 0, 0,
                0, 0, 0, 1, 0, 0, 2, 0,
                0, 0, 0, 2, 0, 0, 0, 0,
                         //
        });
        Assert.assertTrue(board.getOpponentTokens(27, 2, 1)
                .containsAll(Set.of(36, 45, 35, 43, 51)));
    }
}

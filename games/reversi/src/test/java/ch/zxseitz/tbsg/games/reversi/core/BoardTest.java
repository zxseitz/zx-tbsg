package ch.zxseitz.tbsg.games.reversi.core;

import org.junit.Assert;
import org.junit.Test;

public class BoardTest {
    @Test
    public void testGetIndex() {
        Assert.assertEquals(22, Board.getIndex(6, 2));
    }

    @Test
    public void testCovers() {
        var board = new Board();
        Assert.assertTrue(board.covers(3, 3));
        Assert.assertTrue(board.covers(0, 0));
        Assert.assertTrue(board.covers(7, 0));
        Assert.assertTrue(board.covers(0, 7));
        Assert.assertTrue(board.covers(7, 7));
        Assert.assertFalse(board.covers(-1, 0));
        Assert.assertFalse(board.covers(0, -1));
        Assert.assertFalse(board.covers(-1, -1));
        Assert.assertFalse(board.covers(8, 7));
        Assert.assertFalse(board.covers(7, 8));
        Assert.assertFalse(board.covers(8, 8));
    }

    @Test
    public void testGet() {
        var board = new Board();
        Assert.assertEquals(Board.FIELD_UNDEFINED, board.get(-1));
        Assert.assertEquals(Board.FIELD_EMPTY, board.get(0));
        Assert.assertEquals(Board.FIELD_EMPTY, board.get(28));
        Assert.assertEquals(Board.FIELD_EMPTY, board.get(63));
        Assert.assertEquals(Board.FIELD_UNDEFINED, board.get(64));
    }

    @Test
    public void testSet() {
        var board = new Board();
        Assert.assertFalse(board.set(-1, Board.FIELD_EMPTY));
        Assert.assertTrue(board.set(0, Board.FIELD_EMPTY));
        Assert.assertTrue(board.set(63, Board.FIELD_EMPTY));
        Assert.assertFalse(board.set(64, Board.FIELD_EMPTY));
    }
}

package ch.zxseitz.tbsg.games.reversi.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

public class BoardIteratorTest {
    private final Board board;

    public BoardIteratorTest() {
        board = mock(Board.class);
    }

    @Before
    public void setUp() {
        reset(board);
    }

    @Test
    public void testLeftDown() {
        when(board.covers(3, 2)).thenReturn(true);
        when(board.covers(2, 1)).thenReturn(true);
        when(board.covers(1, 0)).thenReturn(true);
        when(board.covers(0, -1)).thenReturn(false);
        when(board.get(19)).thenReturn(Board.FIELD_WHITE);
        when(board.get(10)).thenReturn(Board.FIELD_WHITE);
        when(board.get(1)).thenReturn(Board.FIELD_WHITE);

        var it = new BoardIterator(board);
        it.set(4, 3, -1, -1);
        Assert.assertEquals(Map.entry(19, Board.FIELD_WHITE), it.next());
        Assert.assertEquals(Map.entry(10, Board.FIELD_WHITE), it.next());
        Assert.assertEquals(Map.entry(1, Board.FIELD_WHITE), it.next());
        Assert.assertEquals(Map.entry(-1, Board.FIELD_UNDEFINED), it.next());
    }

    @Test
    public void testRight() {
        when(board.covers(5, 3)).thenReturn(true);
        when(board.covers(6, 3)).thenReturn(true);
        when(board.covers(7, 3)).thenReturn(true);
        when(board.covers(8, 3)).thenReturn(false);
        when(board.get(29)).thenReturn(Board.FIELD_BLACK);
        when(board.get(30)).thenReturn(Board.FIELD_BLACK);
        when(board.get(31)).thenReturn(Board.FIELD_BLACK);

        var it = new BoardIterator(board);
        it.set(4, 3, 1, 0);
        Assert.assertEquals(Map.entry(29, Board.FIELD_BLACK), it.next());
        Assert.assertEquals(Map.entry(30, Board.FIELD_BLACK), it.next());
        Assert.assertEquals(Map.entry(31, Board.FIELD_BLACK), it.next());
        Assert.assertEquals(Map.entry(-1, Board.FIELD_UNDEFINED), it.next());
    }
}

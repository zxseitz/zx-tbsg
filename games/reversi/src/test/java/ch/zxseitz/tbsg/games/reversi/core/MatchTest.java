package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidFieldException;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidPlaceException;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidPlayerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReversiMatch.class, Board.class, BoardIterator.class, ActionCollection.class})
public class MatchTest {
    private final Board board;
    private final BoardIterator iterator;
    private final ActionCollection actionCollection;
    private final IClient black, white;

    public MatchTest() {
        board = mock(Board.class);
        iterator = mock(BoardIterator.class);
        actionCollection = mock(ActionCollection.class);
        black = mock(IClient.class);
        white = mock(IClient.class);
    }

    @Before
    public void setUp() {
        reset(board, iterator, actionCollection, black, white);

        try {
            PowerMockito.whenNew(BoardIterator.class)
                    .withArguments(board).thenReturn(iterator);
            PowerMockito.whenNew(ActionCollection.class)
                    .withNoArguments().thenReturn(actionCollection);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInit() {
        var match = new ReversiMatch("match", black, white, board);
        match.init();

        verify(board, times(1)).set(27, Board.FIELD_WHITE);
        verify(board, times(1)).set(28, Board.FIELD_BLACK);
        verify(board, times(1)).set(35, Board.FIELD_BLACK);
        verify(board, times(1)).set(36, Board.FIELD_WHITE);

        verify(actionCollection, times(1)).add(19, 27);
        verify(actionCollection, times(1)).add(26, 27);
        verify(actionCollection, times(1)).add(37, 36);
        verify(actionCollection, times(1)).add(44, 36);

        Assert.assertEquals(ReversiMatch.STATE_NEXT_BLACK, match.getState());
    }

    @Test
    public void testGetColor() {
        var match = new ReversiMatch("match", black, white, board);
        Assert.assertEquals(1, match.getColor(black));
        Assert.assertEquals(2, match.getColor(white));
        Assert.assertEquals(-1, match.getColor(mock(IClient.class)));
        Assert.assertEquals(-1, match.getColor(null));
    }

    @Test
    public void testPlaceInvalidPlayer() {
        try {
            var match = new ReversiMatch("match", black, white, board);
            match.place(3, 2, 3);
            Assert.fail();
        } catch (InvalidPlayerException ipe) {
            Assert.assertEquals("Unknown color index: 3", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceFinishedGameTie() {
        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_TIE;
            match.place(1, 2, 3);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Match [match] is already finished", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceFinishedGameBlackWon() {
        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_WON_BLACK;
            match.place(1, 2, 3);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Match [match] is already finished", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceFinishedGameWhiteWon() {
        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_WON_WHITE;
            match.place(1, 2, 3);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Match [match] is already finished", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceOpponentsTurn() {
        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_NEXT_BLACK;
            match.place(2, 2, 3);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Not white's turn in match [match]", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceInvalidField() {
        doReturn(false).when(board).covers(-1, -1);

        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_NEXT_BLACK;
            match.place(1, -1, -1);
            Assert.fail();
        } catch (InvalidFieldException ife) {
            Assert.assertEquals("Invalid place action of black on field (-1, -1)" +
            "in match [match]", ife.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceNextBlack() {
        doReturn(true).when(actionCollection).containsIndex(26);
        doReturn(true).when(board).covers(2, 3);

        var field = new int[] {
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 0,
                1, 2, 2, 1, 2, 1, 2, 2,
                1, 1, 1, 1, 1, 1, 2, 0,
                1, 2, 1, 1, 2, 2, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 1, 0
        };
        for (var i = 0; i < field.length; i++) {
            doReturn(field[i]).when(board).get(i);
        }

        doReturn(Map.entry(-1, -1)).when(iterator).next();
        doReturn(true).when(actionCollection).anyIndices();

        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_NEXT_BLACK;
            match.place(1, 2, 3);

            verify(actionCollection, times(1)).foreach(eq(26), any());

            verify(iterator, times(168)).set(anyInt(), anyInt(), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(0), eq(0), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(1), eq(0), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(6), eq(0), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(7), eq(0), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(0), eq(1), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(1), eq(1), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(6), eq(1), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(7), eq(1), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(1), eq(2), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(7), eq(2), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(7), eq(4), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(6), eq(5), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(7), eq(5), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(0), eq(6), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(5), eq(6), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(6), eq(6), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(7), eq(6), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(0), eq(7), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(1), eq(7), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(2), eq(7), anyInt(), anyInt());
            verify(iterator, times(8)).set(eq(7), eq(7), anyInt(), anyInt());

            Assert.assertEquals(ReversiMatch.STATE_NEXT_WHITE, match.getState());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceNextBlackAgain() {
        doReturn(true).when(actionCollection).containsIndex(26);
        doReturn(true).when(board).covers(2, 3);

        var field = new int[] {
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 0,
                1, 2, 2, 1, 2, 1, 2, 2,
                1, 1, 1, 1, 1, 1, 2, 0,
                1, 2, 1, 1, 2, 2, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 1, 0
        };
        for (var i = 0; i < field.length; i++) {
            doReturn(field[i]).when(board).get(i);
        }

        doReturn(Map.entry(-1, -1)).when(iterator).next();
        doReturn(false).doReturn(true)
                .when(actionCollection).anyIndices();

        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_NEXT_BLACK;
            match.place(1, 2, 3);

            verify(actionCollection, times(1)).foreach(eq(26), any());

            verify(iterator, times(336)).set(anyInt(), anyInt(), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(0), eq(0), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(1), eq(0), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(6), eq(0), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(7), eq(0), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(0), eq(1), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(1), eq(1), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(6), eq(1), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(7), eq(1), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(1), eq(2), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(7), eq(2), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(7), eq(4), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(6), eq(5), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(7), eq(5), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(0), eq(6), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(5), eq(6), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(6), eq(6), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(7), eq(6), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(0), eq(7), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(1), eq(7), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(2), eq(7), anyInt(), anyInt());
            verify(iterator, times(16)).set(eq(7), eq(7), anyInt(), anyInt());

            Assert.assertEquals(ReversiMatch.STATE_NEXT_BLACK, match.getState());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceTie() {
        doReturn(true).when(actionCollection).containsIndex(26);
        doReturn(true).when(board).covers(2, 3);

        var field = new int[] {
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 0,
                1, 2, 2, 1, 2, 2, 2, 2,
                1, 1, 1, 1, 1, 1, 2, 0,
                1, 2, 1, 1, 2, 2, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 0, 0
        };
        for (var i = 0; i < field.length; i++) {
            doReturn(field[i]).when(board).get(i);
        }

        doReturn(Map.entry(-1, -1)).when(iterator).next();
        doReturn(false).when(actionCollection).anyIndices();

        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_NEXT_BLACK;
            match.place(1, 2, 3);

            verify(actionCollection, times(1)).foreach(eq(26), any());
            Assert.assertEquals(ReversiMatch.STATE_TIE, match.getState());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testBlackWon() {
        doReturn(true).when(actionCollection).containsIndex(26);
        doReturn(true).when(board).covers(2, 3);

        var field = new int[] {
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 0,
                1, 1, 1, 1, 2, 2, 2, 2,
                1, 1, 1, 1, 1, 1, 2, 0,
                1, 2, 1, 1, 1, 1, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 1, 0
        };
        for (var i = 0; i < field.length; i++) {
            doReturn(field[i]).when(board).get(i);
        }

        doReturn(Map.entry(-1, -1)).when(iterator).next();
        doReturn(false).when(actionCollection).anyIndices();

        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_NEXT_BLACK;
            match.place(1, 2, 3);

            verify(actionCollection, times(1)).foreach(eq(26), any());
            Assert.assertEquals(ReversiMatch.STATE_WON_BLACK, match.getState());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceWhiteWon() {
        doReturn(true).when(actionCollection).containsIndex(26);
        doReturn(true).when(board).covers(2, 3);

        var field = new int[] {
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 2, 2, 2, 2, 2, 0,
                1, 2, 2, 1, 2, 2, 2, 2,
                1, 1, 1, 1, 2, 2, 2, 0,
                1, 2, 2, 2, 2, 2, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 0, 0
        };
        for (var i = 0; i < field.length; i++) {
            doReturn(field[i]).when(board).get(i);
        }

        doReturn(Map.entry(-1, -1)).when(iterator).next();
        doReturn(false).when(actionCollection).anyIndices();

        try {
            var match = new ReversiMatch("match", black, white, board);
            match.state = ReversiMatch.STATE_NEXT_BLACK;
            match.place(1, 2, 3);

            verify(actionCollection, times(1)).foreach(eq(26), any());
            Assert.assertEquals(ReversiMatch.STATE_WON_WHITE, match.getState());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}

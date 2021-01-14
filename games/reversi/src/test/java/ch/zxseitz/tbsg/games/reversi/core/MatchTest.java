package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.EventException;
import ch.zxseitz.tbsg.games.GameState;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.games.IEvent;
import ch.zxseitz.tbsg.games.reversi.Reversi;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidFieldException;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidPlaceException;

import ch.zxseitz.tbsg.games.reversi.exceptions.ReversiException;
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
    private final IClient black, white;
    private final BoardIterator iterator;
    private final ActionCollection actionCollection;

    public MatchTest() {
        board = mock(Board.class);
        black = mock(IClient.class);
        white = mock(IClient.class);
        iterator = mock(BoardIterator.class);
        actionCollection = mock(ActionCollection.class);
    }

    @Before
    public void setUp() {
        reset(board, white, black, iterator, actionCollection);

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
        try {
            var match = new ReversiMatch("match", black, white, board);
            match.init();

            verify(board, times(1)).set(27, Reversi.TOKEN_WHITE);
            verify(board, times(1)).set(28, Reversi.TOKEN_BLACK);
            verify(board, times(1)).set(35, Reversi.TOKEN_BLACK);
            verify(board, times(1)).set(36, Reversi.TOKEN_WHITE);

            verify(actionCollection, times(1)).add(19, 27);
            verify(actionCollection, times(1)).add(26, 27);
            verify(actionCollection, times(1)).add(37, 36);
            verify(actionCollection, times(1)).add(44, 36);

            Assert.assertEquals(GameState.RUNNING, match.getState());
            Assert.assertEquals(Reversi.TOKEN_BLACK, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeNotAMember() {
        try {
            var event = mock(IEvent.class);
            var invader = mock(IClient.class);
            doReturn("invader-id").when(invader).getId();
            var match = new ReversiMatch("match", black, white, board);
            match.invoke(invader, event);
            Assert.fail();
        } catch (ReversiException re) {
            Assert.assertEquals("Client invader-id is not a member of match match", re.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeUnknownCode() {
        try {
            var event = mock(IEvent.class);
            doReturn(-1).when(event).getCode();
            var match = new ReversiMatch("match", black, white, board);
            match.invoke(black, event);
            Assert.fail();
        } catch (EventException ee) {
            Assert.assertEquals("Unknown event code: -1", ee.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeInvalidArg() {
        var missingArgumentException = new EventException("missing argument index");
        try {
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doThrow(missingArgumentException).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.invoke(black, event);
            Assert.fail();
        } catch (EventException ee) {
            Assert.assertEquals(missingArgumentException, ee);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeAborted() {
        try {
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(5).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.ABORTED;
            match.invoke(black, event);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Match [match] is already finished", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeFinished() {
        try {
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(5).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.FINISHED;
            match.invoke(black, event);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Match [match] is already finished", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeNotBlacksTurn() {
        try {
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(5).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_WHITE;
            match.invoke(black, event);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Not black's turn in match [match]", ipe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeInvalidField() {
        try {
            doReturn(false).when(actionCollection).containsIndex(5);
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(5).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, event);
            Assert.fail();
        } catch (InvalidFieldException ife) {
            Assert.assertEquals("Invalid place action of black on field index 5 " +
                    "in match [match]", ife.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


    @Test
    public void testPlaceNextBlack() {
        var field = new int[]{
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

        try {
            doReturn(true).when(actionCollection).containsIndex(26);
            doReturn(true).when(actionCollection).anyIndices();
            doReturn(Map.entry(-1, -1)).when(iterator).next();
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(26).when(event).getArgument("index", Integer.class);

            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, event);

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

            Assert.assertEquals(GameState.RUNNING, match.getState());
            Assert.assertEquals(Reversi.TOKEN_WHITE, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeNextBlackAgain() {
        var field = new int[]{
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

        try {
            doReturn(true).when(actionCollection).containsIndex(26);
            doReturn(false).doReturn(true)
                    .when(actionCollection).anyIndices();
            doReturn(Map.entry(-1, -1)).when(iterator).next();
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(26).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, event);

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

            Assert.assertEquals(GameState.RUNNING, match.getState());
            Assert.assertEquals(Reversi.TOKEN_BLACK, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceTie() {
        var field = new int[]{
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

        try {
            doReturn(true).when(actionCollection).containsIndex(26);
            doReturn(false).when(actionCollection).anyIndices();
            doReturn(Map.entry(-1, -1)).when(iterator).next();
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(26).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, event);

            verify(actionCollection, times(1)).foreach(eq(26), any());
            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(Reversi.TOKEN_EMPTY, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testBlackWon() {
        var field = new int[]{
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

        try {
            doReturn(true).when(actionCollection).containsIndex(26);
            doReturn(false).when(actionCollection).anyIndices();
            doReturn(Map.entry(-1, -1)).when(iterator).next();
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(26).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, event);

            verify(actionCollection, times(1)).foreach(eq(26), any());
            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(Reversi.TOKEN_BLACK, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlaceWhiteWon() {
        var field = new int[]{
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

        try {
            doReturn(true).when(actionCollection).containsIndex(26);
            doReturn(false).when(actionCollection).anyIndices();
            doReturn(Map.entry(-1, -1)).when(iterator).next();
            var event = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(event).getCode();
            doReturn(26).when(event).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, event);

            verify(actionCollection, times(1)).foreach(eq(26), any());
            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(Reversi.TOKEN_WHITE, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}

package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.*;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Reversi.class, ReversiMatch.class, Board.class, ActionCollection.class})
public class MatchTest {
    private final Board board;
    private final IClient black, white;
    private final ActionCollection actionCollection;

    public MatchTest() {
        board = mock(Board.class);
        black = mock(IClient.class);
        white = mock(IClient.class);
        actionCollection = mock(ActionCollection.class);
    }

    @Before
    public void setUp() throws Exception {
        reset(board, white, black, actionCollection);

        PowerMockito.whenNew(ActionCollection.class)
                .withNoArguments().thenReturn(actionCollection);
        PowerMockito.mockStatic(Reversi.class);
    }

    @Test
    public void testInit() throws Exception {
        var field = new int[0];
        var actions = Set.of(19, 26, 37, 44);
        var initBlackEvent = mock(IEvent.class);
        var initWhiteEvent = mock(IEvent.class);
        when(Reversi.createInitPlayerNextEvent(Reversi.TOKEN_BLACK, field, actions.stream()
                .mapToInt(Integer::intValue).toArray())).thenReturn(initBlackEvent);
        when(Reversi.createInitOpponentNextEvent(Reversi.TOKEN_WHITE, field)).thenReturn(initWhiteEvent);
        doReturn(field).when(board).getFields();
        doReturn(actions).when(actionCollection).getIndices();

        var match = new ReversiMatch("match", black, white, board);
        match.init();

        Assert.assertEquals(GameState.RUNNING, match.getState());
        Assert.assertEquals(Reversi.TOKEN_BLACK, match.getNext());
        verify(black, times(1)).invoke(initBlackEvent);
        verify(white, times(1)).invoke(initWhiteEvent);
    }

    @Test
    public void testInvokeNotAMember() throws Exception {
        try {
            var event = mock(IEvent.class);
            var invader = mock(IClient.class);
            doReturn("invader-id").when(invader).getId();
            var match = new ReversiMatch("match", black, white, board);
            match.invoke(invader, event);
            Assert.fail();
        } catch (ReversiException re) {
            Assert.assertEquals("Client invader-id is not a member of match match", re.getMessage());
            verify(black, never()).invoke(any());
            verify(white, never()).invoke(any());
        }
    }

    @Test
    public void testInvokeUnknownCode() throws ClientException {
        try {
            var event = mock(IEvent.class);
            doReturn(-1).when(event).getCode();
            var match = new ReversiMatch("match", black, white, board);
            match.invoke(black, event);
            Assert.fail();
        } catch (EventException ee) {
            Assert.assertEquals("Unknown event code: -1", ee.getMessage());
            verify(black, never()).invoke(any());
            verify(white, never()).invoke(any());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokeInvalidArg() throws Exception {
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
            verify(black, never()).invoke(any());
            verify(white, never()).invoke(any());
        }
    }

    @Test
    public void testInvokeAborted() throws Exception {
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
            verify(black, never()).invoke(any());
            verify(white, never()).invoke(any());
        }
    }

    @Test
    public void testInvokeFinished() throws Exception {
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
            verify(black, never()).invoke(any());
            verify(white, never()).invoke(any());
        }
    }

    @Test
    public void testInvokeNotBlacksTurn() throws Exception {
        try {
            var clientEvent = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(clientEvent).getCode();
            doReturn(5).when(clientEvent).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_WHITE;
            match.invoke(black, clientEvent);
            Assert.fail();
        } catch (InvalidPlaceException ipe) {
            Assert.assertEquals("Not black's turn in match [match]", ipe.getMessage());
            verify(black, never()).invoke(any());
            verify(white, never()).invoke(any());
        }
    }

    @Test
    public void testInvokeInvalidField() throws Exception {
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
            verify(black, never()).invoke(any());
            verify(white, never()).invoke(any());
        }
    }

    @Test
    public void testInvokePlaceBlackNextWhite() {
        var indexBlack = 39;
        var indexNextWhite = 58;
        var actionsBlack = Set.of(38, 37, 36);
        var actionsNextWhite = Set.of(50, 42, 34);
        var previewWhite = Set.of(2, 6, 9, 14, 17, 53, 58);
        var fields = new int[]{
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 2,
                1, 2, 2, 1, 2, 1, 2, 2,
                1, 1, 1, 1, 2, 2, 2, 0,
                1, 2, 1, 1, 2, 2, 0, 1,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 1, 0
        };

        try {
            for (var i = 0; i < fields.length; i++) {
                doReturn(fields[i]).when(board).get(i);
            }
            doReturn(fields).when(board).getFields();
            doReturn(actionsNextWhite).when(board).getOpponentTokens(indexNextWhite, Reversi.TOKEN_WHITE, Reversi.TOKEN_BLACK);
            doReturn(true).when(actionCollection).containsIndex(indexBlack);
            doReturn(true).when(actionCollection).anyIndices();
            doReturn(actionsBlack).when(actionCollection).get(indexBlack);
            doReturn(previewWhite).when(actionCollection).getIndices();

            var nextBlackEvent = mock(IEvent.class);
            var nextWhiteEvent = mock(IEvent.class);
            when(Reversi.createOpponentNextEvent(eq(indexBlack), eq(fields))).thenReturn(nextBlackEvent);
            when(Reversi.createPlayerNextEvent(eq(indexBlack),eq(fields), eq(previewWhite.stream()
                    .mapToInt(Integer::intValue).toArray()))).thenReturn(nextWhiteEvent);

            var clientEvent = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(clientEvent).getCode();
            doReturn(indexBlack).when(clientEvent).getArgument("index", Integer.class);

            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, clientEvent);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, Reversi.TOKEN_BLACK);
            verify(board, times(1)).set(actionsBlack, Reversi.TOKEN_BLACK);

            // verify new white actions
            verify(actionCollection, times(1)).add(eq(indexNextWhite), eq(actionsNextWhite));

            Assert.assertEquals(GameState.RUNNING, match.getState());
            Assert.assertEquals(Reversi.TOKEN_WHITE, match.getNext());

            // verify client messages
            verify(black, times(1)).invoke(nextBlackEvent);
            verify(white, times(1)).invoke(nextWhiteEvent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceBlackNextBlack() {
        var indexBlack = 39;
        var indexNextBlack = 58;
        var actionsBlack = Set.of(38, 37, 36);
        var actionsNextBlack = Set.of(50, 42, 34);
        var previewBlack = Set.of(2, 6, 9, 14, 17, 53, 58);
        var fields = new int[]{
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 2,
                1, 2, 2, 1, 2, 1, 2, 2,
                1, 1, 1, 1, 2, 2, 2, 0,
                1, 2, 1, 1, 2, 2, 0, 1,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 1, 0
        };

        try {
            for (var i = 0; i < fields.length; i++) {
                doReturn(fields[i]).when(board).get(i);
            }
            doReturn(fields).when(board).getFields();
            doReturn(actionsNextBlack).when(board).getOpponentTokens(indexNextBlack, Reversi.TOKEN_WHITE, Reversi.TOKEN_BLACK);
            doReturn(true).when(actionCollection).containsIndex(indexBlack);
            doReturn(false).doReturn(true).when(actionCollection).anyIndices();
            doReturn(actionsBlack).when(actionCollection).get(indexBlack);
            doReturn(previewBlack).when(actionCollection).getIndices();

            var nextBlackEvent = mock(IEvent.class);
            var nextWhiteEvent = mock(IEvent.class);
            when(Reversi.createPlayerNextEvent(eq(indexBlack),eq(fields), eq(previewBlack.stream()
                    .mapToInt(Integer::intValue).toArray()))).thenReturn(nextBlackEvent);
            when(Reversi.createOpponentNextEvent(eq(indexBlack), eq(fields))).thenReturn(nextWhiteEvent);

            var clientEvent = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(clientEvent).getCode();
            doReturn(indexBlack).when(clientEvent).getArgument("index", Integer.class);

            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, clientEvent);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, Reversi.TOKEN_BLACK);
            verify(board, times(1)).set(actionsBlack, Reversi.TOKEN_BLACK);

            // verify new black actions
            verify(actionCollection, times(1)).add(eq(indexNextBlack), eq(actionsNextBlack));

            Assert.assertEquals(GameState.RUNNING, match.getState());
            Assert.assertEquals(Reversi.TOKEN_BLACK, match.getNext());

            // verify client messages
            verify(black, times(1)).invoke(nextBlackEvent);
            verify(white, times(1)).invoke(nextWhiteEvent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceBlackWon() {
        var indexBlack = 39;
        var actionsBlack = Set.of(38, 37, 36);
        var fields = new int[]{
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 0,
                1, 1, 1, 1, 2, 2, 2, 2,
                1, 1, 1, 1, 1, 1, 2, 0,
                1, 2, 1, 1, 1, 1, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 1, 0
        };

        try {
            for (var i = 0; i < fields.length; i++) {
                doReturn(fields[i]).when(board).get(i);
            }
            doReturn(fields).when(board).getFields();
            doReturn(true).when(actionCollection).containsIndex(indexBlack);
            doReturn(actionsBlack).when(actionCollection).get(indexBlack);
            doReturn(false).when(actionCollection).anyIndices();

            var blackWonEvent = mock(IEvent.class);
            var whiteDefeatEvent = mock(IEvent.class);
            when(Reversi.createVictoryEvent(eq(indexBlack),eq(fields))).thenReturn(blackWonEvent);
            when(Reversi.createDefeatEvent(eq(indexBlack), eq(fields))).thenReturn(whiteDefeatEvent);

            var clientEvent = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(clientEvent).getCode();
            doReturn(indexBlack).when(clientEvent).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, clientEvent);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, Reversi.TOKEN_BLACK);
            verify(board, times(1)).set(actionsBlack, Reversi.TOKEN_BLACK);

            verify(actionCollection, never()).add(anyInt(), anyCollection());

            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(Reversi.TOKEN_BLACK, match.getNext());

            verify(black, times(1)).invoke(blackWonEvent);
            verify(white, times(1)).invoke(whiteDefeatEvent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceWhiteWon() {
        var indexBlack = 39;
        var actionsBlack = Set.of(38, 37, 36);
        var fields = new int[]{
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 2, 2, 2, 2, 2, 0,
                1, 2, 2, 1, 2, 2, 2, 2,
                1, 1, 1, 1, 2, 2, 2, 0,
                1, 2, 2, 2, 2, 2, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 0, 0
        };

        try {
            for (var i = 0; i < fields.length; i++) {
                doReturn(fields[i]).when(board).get(i);
            }
            doReturn(fields).when(board).getFields();
            doReturn(true).when(actionCollection).containsIndex(indexBlack);
            doReturn(actionsBlack).when(actionCollection).get(indexBlack);
            doReturn(false).when(actionCollection).anyIndices();

            var blackDefeatEvent = mock(IEvent.class);
            var whiteWonEvent = mock(IEvent.class);
            when(Reversi.createDefeatEvent(eq(indexBlack), eq(fields))).thenReturn(blackDefeatEvent);
            when(Reversi.createVictoryEvent(eq(indexBlack),eq(fields))).thenReturn(whiteWonEvent);

            var clientEvent = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(clientEvent).getCode();
            doReturn(indexBlack).when(clientEvent).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, clientEvent);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, Reversi.TOKEN_BLACK);
            verify(board, times(1)).set(actionsBlack, Reversi.TOKEN_BLACK);

            verify(actionCollection, never()).add(anyInt(), anyCollection());

            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(Reversi.TOKEN_WHITE, match.getNext());

            verify(black, times(1)).invoke(blackDefeatEvent);
            verify(white, times(1)).invoke(whiteWonEvent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceTie() {
        var indexBlack = 39;
        var actionsBlack = Set.of(38, 37, 36);
        var fields = new int[]{
                0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 2, 2, 2, 1, 0, 0,
                1, 0, 1, 1, 2, 2, 2, 0,
                1, 2, 2, 1, 2, 2, 2, 2,
                1, 1, 1, 1, 1, 1, 2, 0,
                1, 2, 1, 1, 2, 2, 0, 0,
                0, 2, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 1, 1, 1, 0, 0
        };

        try {
            for (var i = 0; i < fields.length; i++) {
                doReturn(fields[i]).when(board).get(i);
            }
            doReturn(fields).when(board).getFields();
            doReturn(true).when(actionCollection).containsIndex(indexBlack);
            doReturn(actionsBlack).when(actionCollection).get(indexBlack);
            doReturn(false).when(actionCollection).anyIndices();

            var tieEvent = mock(IEvent.class);
            when(Reversi.createTieEvent(eq(indexBlack), eq(fields))).thenReturn(tieEvent);

            var clientEvent = mock(IEvent.class);
            doReturn(Reversi.CLIENT_PLACE).when(clientEvent).getCode();
            doReturn(indexBlack).when(clientEvent).getArgument("index", Integer.class);
            var match = new ReversiMatch("match", black, white, board);
            match.state = GameState.RUNNING;
            match.next = Reversi.TOKEN_BLACK;
            match.invoke(black, clientEvent);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, Reversi.TOKEN_BLACK);
            verify(board, times(1)).set(actionsBlack, Reversi.TOKEN_BLACK);

            verify(actionCollection, never()).add(anyInt(), anyCollection());

            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(Reversi.TOKEN_EMPTY, match.getNext());

            verify(black, times(1)).invoke(tieEvent);
            verify(white, times(1)).invoke(tieEvent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}

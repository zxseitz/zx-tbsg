package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.exceptions.GameException;
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
@PrepareForTest({Reversi.class, Board.class, Action.class,})
public class ReversiTest {
    private final Board board;
    private final Map<Action, Collection<Integer>> actions;

    public ReversiTest() {
        board = mock(Board.class);
        actions = mock(Map.class);
    }

    @Before
    public void setUp() {
        reset(board, actions);
    }

    @Test
    public void testUpdateNull() {
        try {
            var match = new Reversi(board, actions);
            match.update(null);
            Assert.fail();
        } catch (GameException ge) {
            Assert.assertEquals("Action is null", ge.getMessage());
        }
    }

    @Test
    public void testUpdateInvalidIndex() throws Exception {
        try {
            var action = mock(Action.class);
            doReturn(null).when(actions).get(action);
            var match = new Reversi(board, actions);
            match.update(action);
            Assert.fail();
        } catch (GameException ge) {
            Assert.assertEquals("Index defined in action is invalid", ge.getMessage());
        }
    }

    @Test
    public void testUpdatePlaceBlackNextWhite() {
        var indexBlack = 39;
        var indexNextWhite = 58;
        var actionBlack = mock(Action.class);
        var actionWhite = mock(Action.class);
        var actionsBlack = Set.of(38, 37, 36);
        var actionsNextWhite = Set.of(50, 42, 34);
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
            doReturn(indexBlack).when(actionBlack).getIndex();
            doReturn(fields).when(board).getFields();
            doReturn(actionsNextWhite).when(board).getOpponentTokens(indexNextWhite, 2, 1);
            doReturn(actionsBlack).when(actions).get(actionBlack);
            doReturn(1).when(actions).size();
            PowerMockito.whenNew(Action.class).withArguments(eq(indexNextWhite)).thenReturn(actionWhite);

            var match = new Reversi(board, actions);
            match.state = GameState.RUNNING;
            match.next = 1;
            match.update(actionBlack);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, 1);
            verify(board, times(1)).set(actionsBlack, 1);

            // verify new white actions
            verify(actions, times(1)).put(eq(actionWhite), eq(actionsNextWhite));

            Assert.assertEquals(GameState.RUNNING, match.getState());
            Assert.assertEquals(2, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceBlackNextBlack() {
        var indexBlack = 39;
        var indexNextBlack = 58;
        var blackAction = mock(Action.class);
        var blackNextAction = mock(Action.class);
        var actionsBlack = Set.of(38, 37, 36);
        var actionsNextBlack = Set.of(50, 42, 34);
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
            doReturn(indexBlack).when(blackAction).getIndex();
            doReturn(indexNextBlack).when(blackNextAction).getIndex();
            doReturn(fields).when(board).getFields();
            doReturn(actionsNextBlack).when(board).getOpponentTokens(indexNextBlack, 2, 1);
            doReturn(0).doReturn(1).when(actions).size();
            doReturn(actionsBlack).when(actions).get(blackAction);
            PowerMockito.whenNew(Action.class).withArguments(eq(indexNextBlack)).thenReturn(blackNextAction);

            var match = new Reversi(board, actions);
            match.state = GameState.RUNNING;
            match.next = 1;
            match.update(blackAction);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, 1);
            verify(board, times(1)).set(actionsBlack, 1);

            // verify new black actions
            verify(actions, times(1)).put(eq(blackNextAction), eq(actionsNextBlack));

            Assert.assertEquals(GameState.RUNNING, match.getState());
            Assert.assertEquals(1, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceBlackWon() {
        var indexBlack = 39;
        var actionBlack = mock(Action.class);
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
            doReturn(indexBlack).when(actionBlack).getIndex();
            doReturn(actionsBlack).when(actions).get(actionBlack);
            doReturn(0).when(actions).size();

            var match = new Reversi(board, actions);
            match.state = GameState.RUNNING;
            match.next = 1;
            match.update(actionBlack);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, 1);
            verify(board, times(1)).set(actionsBlack, 1);

            verify(actions, never()).put(any(), anyCollection());

            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(1, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceWhiteWon() {
        var indexBlack = 39;
        var actionBlack = mock(Action.class);
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
            doReturn(indexBlack).when(actionBlack).getIndex();
            doReturn(actionsBlack).when(actions).get(actionBlack);
            doReturn(0).when(actions).size();

            var match = new Reversi(board, actions);
            match.state = GameState.RUNNING;
            match.next = 1;
            match.update(actionBlack);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, 1);
            verify(board, times(1)).set(actionsBlack, 1);

            verify(actions, never()).put(any(), anyCollection());

            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(2, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testInvokePlaceTie() {
        var indexBlack = 39;
        var actionBlack = mock(Action.class);
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
            doReturn(indexBlack).when(actionBlack).getIndex();
            doReturn(actionsBlack).when(actions).get(actionBlack);
            doReturn(0).when(actions).size();

            var match = new Reversi(board, actions);
            match.state = GameState.RUNNING;
            match.next = 1;
            match.update(actionBlack);

            // verify black board changes
            verify(board, times(1)).set(indexBlack, 1);
            verify(board, times(1)).set(actionsBlack, 1);

            verify(actions, never()).put(any(), anyCollection());

            Assert.assertEquals(GameState.FINISHED, match.getState());
            Assert.assertEquals(0, match.getNext());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}

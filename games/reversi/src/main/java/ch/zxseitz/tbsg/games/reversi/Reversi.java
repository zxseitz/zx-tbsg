package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;
import ch.zxseitz.tbsg.games.annotations.Color;
import ch.zxseitz.tbsg.games.exceptions.GameException;

import java.util.*;

@TbsgGame(name = "reversi", colors = {
        @Color(value = 1, name = "black"),
        @Color(value = 2, name = "white"),
})
public class Reversi implements IGame<Action> {
    private static synchronized String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public int compareTo(IGame<?> o) {
        return id.compareTo(o.getId());
    }

    private final String id;
    private final Board board;
    private final Map<Action, Collection<Integer>> actions;
    int next; //accessible for testing
    GameState state; //accessible for testing

    public Reversi() {
        this.id = generateId();
        this.board = new Board();
        this.actions = new TreeMap<>();
    }

    /**
     * Test constructor
     *
     * @param board
     * @param actions
     */
    public Reversi(Board board, Map<Action, Collection<Integer>> actions) {
        this.id = generateId();
        this.board = board;
        this.actions = actions;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getNext() {
        return next;
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public int[] getBoard() {
        return board.getFields();
    }

    @Override
    public Collection<Action> getPreview() {
        return actions.keySet();
    }

    @Override
    public void update(Action action) throws GameException {
        if (action == null) {
            throw new GameException("Action is null");
        }
        var collection = actions.get(action);
        if (collection == null) {
            throw new GameException("Index defined in action is invalid");
        }
        board.set(action.getIndex(), next);
        board.set(collection, next);
        var opponentColor = 3 - next;

        // update state and actions
        actions.clear();
        var emptyFields = new TreeSet<Integer>();
        var blackCount = 0;
        var whiteCount = 0;
        for (var i = 0; i < 64; i++) {
            switch (board.get(i)) {
                case 1:
                    blackCount++;
                    break;
                case 2:
                    whiteCount++;
                    break;
                default:
                    emptyFields.add(i);
            }
        }

        // check next opponent turn
        addActions(emptyFields, opponentColor, next);
        if (actions.size() > 0) {
            this.next = opponentColor;
            return;
        }

        // check next own turn, if opponent has no legal moves
        addActions(emptyFields, next, opponentColor);
        if (actions.size() > 0) {
            return;
        }

        // check end state, if no one has legal moves
        this.state = GameState.FINISHED;
        if (blackCount > whiteCount) {
            this.next = 1;
        } else if (blackCount < whiteCount) {
            this.next = 2;
        } else {
            this.next = 0;
        }
    }

    private void addActions(Set<Integer> emptyFields, int color, int opponentColor) {
        for (var ai : emptyFields) {
            var actions = board.getOpponentTokens(ai, color, opponentColor);
            if (actions.size() > 0) {
                this.actions.put(new Action(ai), actions);
            }
        }
    }
}

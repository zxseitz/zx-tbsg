package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidFieldException;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidPlaceException;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidPlayerException;
import ch.zxseitz.tbsg.games.reversi.exceptions.ReversiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ReversiMatch implements IMatch {
    public static final int STATE_UNDEFINED = -1;
    public static final int STATE_NEXT_BLACK = 1;
    public static final int STATE_NEXT_WHITE = 2;
    public static final int STATE_TIE = 10;
    public static final int STATE_WON_BLACK = 11;
    public static final int STATE_WON_WHITE = 12;

    public static final int CODE_PLAYER_PLACE = 1010;

    public static final int CODE_SERVER_OPPONENT_DISCONNECTED = 2020;
    public static final int CODE_SERVER_INIT = 2100;
    public static final int CODE_SERVER_UPDATE = 2110;

    private final String id;
    private final IClient black, white;
    private final List<Audit> history;
    private final Board board;
    private final ActionCollection actionCollection;

    int state;  //accessible for testing

    public ReversiMatch(String id, IClient black, IClient white, Board board) {
        this.id = id;
        this.black = black;
        this.white = white;
        this.board = board;
        this.history = new ArrayList<>(60);
        this.actionCollection = new ActionCollection();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void resign(IClient client) {
        //todo
    }

    @Override
    public int getClientPos(IClient client) {
        return client.equals(black) ? 1 : client.equals(white) ? 2 : -1;
    }

    @Override
    public IClient getClient(int pos) {
        return pos == 1 ? black : pos == 2 ? white : null;
    }

    @Override
    public void init() throws ClientException {
        // set board initial state (othello)
        board.set(27, 2);
        board.set(28, 1);
        board.set(35, 1);
        board.set(36, 2);

        // first black actions
        actionCollection.add(19, 27);
        actionCollection.add(26, 27);
        actionCollection.add(37, 36);
        actionCollection.add(44, 36);

        state = STATE_NEXT_BLACK;

        black.invoke(createInitEvent(1));
        white.invoke(createInitEvent(2));
    }

//    @ClientEvent(CODE_PLAYER_PLACE)
//    public void placeEvent(IClient sender, @EventArg("x") int x, @EventArg("y") int y) throws GameException {
//        // todo player
//        place(clientPos, x, y);
//        history.add(new Audit(clientPos, Board.getIndex(x, y)));
//        black.invoke(createUpdateEvent(1)); //todo correct status
//        white.invoke(createUpdateEvent(0)); //todo correct status
//    }

    @Override
    public void action(IClient sender, IEvent event) throws ClientException, EventException, GameException {
        var clientPos = getClientPos(sender);
        if (clientPos < 0) {
            throw new EventException("Client " + sender.getId() + " is not a member of match " + id);
        }
        switch (event.getCode()) {
            case CODE_PLAYER_PLACE:
                var x = event.getArgument("x", Integer.class);
                var y = event.getArgument("y", Integer.class);
                place(clientPos, x, y);
                history.add(new Audit(clientPos, Board.getIndex(x, y)));
                black.invoke(createUpdateEvent(1)); //todo correct status
                white.invoke(createUpdateEvent(0)); //todo correct status
                break;
            default:
                throw new EventException("Invalid event code: " + event.getCode());
        }
    }

    private IEvent createInitEvent(int color) {
        var event = new Event(CODE_SERVER_INIT);
        event.addArgument("color", color);
        event.addArgument("board", board);
        return event;
    }

    private IEvent createUpdateEvent(int status) {
        var event = new Event(CODE_SERVER_UPDATE);
        event.addArgument("status", status);
        event.addArgument("board", board);
        return event;
    }

    /**
     * Returns the current state of this match.
     *
     * @return <code>1</code> black's turn, <code>2</code> white's turn,
     * <code>10</code> tie, <code>11</code> black won or <code>12</code> white won.
     */
    int getState() {
        return state;
    }

    List<Audit> getHistory() {
        return history;
    }

    String getColorName(int color) {
        return color == 1 ? "black" : color == 2 ? "white" : "unknown";
    }

    /**
     * Places a new token of a player on a field and updates the internal state.
     *
     * @param color acteur
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @throws InvalidPlayerException if the player is not a member of this match
     * @throws InvalidPlaceException  if the place action is not valid
     * @throws InvalidFieldException  if the field position is invalid
     */
    void place(int color, int x, int y) throws ReversiException {
        var opponentColor = 3 - color;
        // check current state
        if (state >= STATE_TIE) {
            throw new InvalidPlaceException(String.format("Match [%s] is already finished", id));
        }
        if (color != state) {
            throw new InvalidPlaceException(String.format("Not %s's turn in match [%s]", getColorName(color), id));
        }
        var index = board.covers(x, y) ? Board.getIndex(x, y) : -1;
        if (!actionCollection.containsIndex(index)) {
            throw new InvalidFieldException(String.format("Invalid place action of %s on field (%d, %d)" +
                    "in match [%s]", getColorName(color), x, y, id));
        }

        // apply new token
        board.set(index, color);
        actionCollection.foreach(index, i -> {
            board.set(i, color);
        });

        // update state and actions
        actionCollection.clear();
        var emptyFields = new TreeSet<Integer>();
        var blackCount = 0;
        var whiteCount = 0;
        for (var i = 0; i < 64; i++) {
            switch (board.get(i)) {
                case Board.FIELD_BLACK:
                    blackCount++;
                    break;
                case Board.FIELD_WHITE:
                    whiteCount++;
                    break;
                default:
                    emptyFields.add(i);
            }
        }

        // check next opponent turn
        addActions(emptyFields, opponentColor);
        if (actionCollection.anyIndices()) {
            this.state = opponentColor;
            return;
        }

        // check next own turn, if opponent has no legal moves
        addActions(emptyFields, color);
        if (actionCollection.anyIndices()) {
            this.state = color;
            return;
        }

        // check end state, if no one has legal moves
        if (blackCount > whiteCount) {
            this.state = STATE_WON_BLACK;
        } else if (blackCount < whiteCount) {
            this.state = STATE_WON_WHITE;
        } else {
            this.state = STATE_TIE;
        }
    }

    private void addActions(Set<Integer> emptyFields, int color) {
        var it = new BoardIterator(board);
        for (var ai : emptyFields) {
            var ax = ai % 8;
            var ay = ai / 8;
            var set = new TreeSet<Integer>();
            set.addAll(iterateStraight(it, ax, ay, 1, 0, color));
            set.addAll(iterateStraight(it, ax, ay, 1, 1, color));
            set.addAll(iterateStraight(it, ax, ay, 0, 1, color));
            set.addAll(iterateStraight(it, ax, ay, -1, 1, color));
            set.addAll(iterateStraight(it, ax, ay, -1, 0, color));
            set.addAll(iterateStraight(it, ax, ay, -1, -1, color));
            set.addAll(iterateStraight(it, ax, ay, 0, -1, color));
            set.addAll(iterateStraight(it, ax, ay, 1, -1, color));
            actionCollection.add(ai, set);
        }
    }

    private Set<Integer> iterateStraight(BoardIterator iterator, int x, int y, int dx, int dy, int color) {
        var fields = new TreeSet<Integer>();
        var opponentColor = 3 - color;
        iterator.set(x, y, dx, dy);
        var state = iterator.next();
        while (state.getValue() == opponentColor) {
            fields.add(state.getKey());
            state = iterator.next();
        }
        if (state.getKey() <= 0) {
            fields.clear();
        }
        return fields;
    }

    public String printBoard() {
        return board.toString();
    }
}

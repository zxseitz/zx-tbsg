package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.ClientException;
import ch.zxseitz.tbsg.games.EventException;
import ch.zxseitz.tbsg.games.GameException;
import ch.zxseitz.tbsg.games.GameState;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.games.IEvent;
import ch.zxseitz.tbsg.games.IMatch;
import ch.zxseitz.tbsg.games.reversi.Reversi;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidFieldException;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidPlaceException;
import ch.zxseitz.tbsg.games.reversi.exceptions.InvalidPlayerException;
import ch.zxseitz.tbsg.games.reversi.exceptions.ReversiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class ReversiMatch implements IMatch {
    private final String id;
    private final Map<IClient, Integer> players;
    private final List<Audit> history;
    private final Board board;
    private final ActionCollection actionCollection;

    GameState state;
    int next;  //accessible for testing

    public ReversiMatch(String id, IClient black, IClient white, Board board) {
        this.id = id;
        this.players = new HashMap<>();
        this.players.put(black, Board.FIELD_BLACK);
        this.players.put(white, Board.FIELD_WHITE);
        this.board = board;
        this.history = new ArrayList<>(60);
        this.actionCollection = new ActionCollection();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int compareTo(IMatch o) {
        return id.compareTo(o.getId());
    }

    @Override
    public void resign(IClient sender) throws ClientException, GameException {
        var color = players.get(sender);
        if (color == null) {
            throw new ReversiException("Client " + sender.getId() + " is not a member of match " + id);
        }
        var fields = board.getFields();
        state = GameState.ABORTED;
        if (color == Board.FIELD_BLACK) {
            // black resigns
            next = Board.FIELD_WHITE;
            emitAll(Reversi.createDefeatEvent(-1, fields),
                    Reversi.createVictoryEvent(-1, fields));
        } else if (color == Board.FIELD_WHITE) {
            // white resigns
            next = Board.FIELD_BLACK;
            emitAll(Reversi.createVictoryEvent(-1, fields),
                    Reversi.createDefeatEvent(-1, fields));
        }
    }

    @Override
    public Set<IClient> getPlayers() {
        return players.keySet();
    }

    @Override
    public void init() throws ClientException, GameException {
        if (state != null) {
            throw new ReversiException("Match " + id + " was already initialized");
        }

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

        state = GameState.RUNNING;
        next = Board.FIELD_BLACK;

        var fields = board.getFields();
        var preview = actionCollection.getIndices().stream().mapToInt(Number::intValue).toArray();
        emitAll(Reversi.createInitPlayerNextEvent(1, fields, preview),
                Reversi.createInitOpponentNextEvent(2, fields));
    }

    void emitAll(IEvent blackEvent, IEvent whiteEvent) {
        players.forEach((client, integer) -> {
            try {
                client.invoke(integer == Board.FIELD_BLACK ? blackEvent : integer == Board.FIELD_WHITE ? whiteEvent : null);
            } catch (Exception ignore) {
                // inform opponent?
            }
        });
    }

    Optional<IClient> getOpponent(IClient client) {
        return players.keySet().stream()
                .filter(client1 -> !client1.equals(client))
                .findFirst();
    }

    @Override
    public void invoke(IClient sender, IEvent event) throws ClientException, EventException, GameException {
        var o = getOpponent(sender);
        if (o.isEmpty()) {
            throw new ReversiException("Client " + sender.getId() + " is not a member of match " + id);
        }
        switch (event.getCode()) {
            case Reversi.CLIENT_PLACE:
                var index = event.getArgument("index", Integer.class);
                var opponent = o.get();
                var color = players.get(sender);
                var opponentColor = players.get(opponent);
                place(color, opponentColor, index);
                history.add(new Audit(sender, index));
                var fields = board.getFields();
                var preview = actionCollection.getIndices().stream().mapToInt(Number::intValue).toArray();
                if (state != GameState.RUNNING) {
                    var playerNextEvent = Reversi.createPlayerNextEvent(index, fields, preview);
                    var opponentNextEvent = Reversi.createOpponentNextEvent(index, fields);
                    sender.invoke(next == color ? playerNextEvent : opponentNextEvent);
                    opponent.invoke(next == opponentColor ? playerNextEvent : opponentNextEvent);
                } else {
                    if (next == color) {
                        sender.invoke(Reversi.createVictoryEvent(index, fields));
                        opponent.invoke(Reversi.createDefeatEvent(index, fields));
                    } else if (next == opponentColor) {
                        sender.invoke(Reversi.createDefeatEvent(index, fields));
                        opponent.invoke(Reversi.createVictoryEvent(index, fields));
                    } else {
                        var tieEvent = Reversi.createTieEvent(index, fields);
                        sender.invoke(tieEvent);
                        opponent.invoke(tieEvent);
                    }
                }
                break;
            default:
                throw new EventException("Invalid event code: " + event.getCode());
        }
    }

    public GameState getState() {
        return state;
    }

    public GameState getNext() {
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
     * @param color         player color
     * @param opponentColor opponent color
     * @param index         token index
     * @throws InvalidPlayerException if the player is not a member of this match
     * @throws InvalidPlaceException  if the place action is not valid
     * @throws InvalidFieldException  if the field position is invalid
     */
    void place(int color, int opponentColor, int index) throws ReversiException {
        // check current state
        if (state != GameState.RUNNING) {
            throw new InvalidPlaceException(String.format("Match [%s] is already finished", id));
        }
        if (color != next) {
            throw new InvalidPlaceException(String.format("Not %s's turn in match [%s]", getColorName(color), id));
        }
        if (!actionCollection.containsIndex(index)) {
            throw new InvalidFieldException(String.format("Invalid place action of %s on field index %d" +
                    "in match [%s]", getColorName(color), index, id));
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
            this.next = opponentColor;
            return;
        }

        // check next own turn, if opponent has no legal moves
        addActions(emptyFields, color);
        if (actionCollection.anyIndices()) {
            this.next = color;
            return;
        }

        // check end state, if no one has legal moves
        this.state = GameState.FINISHED;
        if (blackCount > whiteCount) {
            this.next = Board.FIELD_BLACK;
        } else if (blackCount < whiteCount) {
            this.next = Board.FIELD_WHITE;
        } else {
            this.next = Board.FIELD_UNDEFINED;
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


//    @ClientEvent(CODE_PLAYER_PLACE)
//    public void placeEvent(IClient sender, @EventArg("x") int x, @EventArg("y") int y) throws GameException {
//        // todo player
//        place(clientPos, x, y);
//        history.add(new Audit(clientPos, Board.getIndex(x, y)));
//        black.invoke(createUpdateEvent(1)); //todo correct status
//        white.invoke(createUpdateEvent(0)); //todo correct status
//    }

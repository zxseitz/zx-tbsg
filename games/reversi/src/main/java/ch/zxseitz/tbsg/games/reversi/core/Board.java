package ch.zxseitz.tbsg.games.reversi.core;


import ch.zxseitz.tbsg.games.reversi.Reversi;

import java.util.*;

public class Board {
    private static final int SIZE = 64;
    private static final Collection<Integer> EMPTY_INTS = Collections.emptyList();

    /**
     * Calculates the field index.
     *
     * @param x - field x coordinate
     * @param y - field y coordinate
     * @return index value
     */
    public static int getIndex(int x, int y) {
        return y * 8 + x;
    }

    private final int[] fields;

    /**
     * Constructor
     */
    public Board() {
        this.fields = new int[SIZE];
    }

    /**
     * Test constructor
     * @param fields
     */
    public Board(int[] fields) {
        this();
        if (fields.length != SIZE) {
            throw new IllegalArgumentException("Input array length is " + fields.length + ", expected " + SIZE);
        }
        System.arraycopy(fields, 0, this.fields, 0, SIZE);
    }

    /**
     * Checks if a field coordinate is on this board
     *
     * @param x - field x coordinate
     * @param y - field y coordinate
     * @return <code>true</code> if the field coordinate is on this board,
     * or <code>false</code> otherwise
     */
    public static boolean covers(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    /**
     * Gets the state of a specific field.
     *
     * @param index field index
     * @return field state
     */
    public int get(int index) {
        if (index >= 0 && index < SIZE) {
            return fields[index];
        }
        return Reversi.TOKEN_UNDEFINED;
    }

    /**
     * Sets the state of a specific field.
     *
     * @param index field index
     * @param state field state
     * @return <code>true</code> if the state was set successfully,
     *   or <code>false</code> otherwise
     */
    public boolean set(int index, int state) {
        if (index >= 0 && index < 64) {
            fields[index] = state;
            return true;
        }
        return false;
    }

    public void set(Collection<Integer> fields, int state) {
        fields.forEach(field -> this.fields[field] = state);
    }

    public Collection<Integer> getOpponentTokens(int index, int state, int stateOpponent) {
        var tokens = new ArrayList<Integer>();
        var x = index % 8;
        var y = index / 8;
        tokens.addAll(iterate(x, y, 1, 0, state, stateOpponent));
        tokens.addAll(iterate(x, y, 1, 1, state, stateOpponent));
        tokens.addAll(iterate(x, y, 0, 1, state, stateOpponent));
        tokens.addAll(iterate(x, y, -1, 1, state, stateOpponent));
        tokens.addAll(iterate(x, y, -1, 0, state, stateOpponent));
        tokens.addAll(iterate(x, y, -1, -1, state, stateOpponent));
        tokens.addAll(iterate(x, y, 0, -1, state, stateOpponent));
        tokens.addAll(iterate(x, y, 1, -1, state, stateOpponent));
        return tokens;
    }

    private Collection<Integer> iterate(int x, int y, int dx, int dy, int color, int opponentColor) {
        var fields = new ArrayList<Integer>();
        var ix = x + dx;
        var iy = y + dy;
        var c = get(ix, iy);
        while (c == opponentColor)  {
            fields.add(Board.getIndex(ix, iy));
            ix += dx;
            iy += dy;
            c = get(ix, iy);
        }
        return  c == color ? fields : EMPTY_INTS;
    }

    public int get(int x, int y) {
        return covers(x, y) ? fields[getIndex(x, y)] : Reversi.TOKEN_UNDEFINED;
    }

    public int[] getFields() {
        var result = new int[SIZE];
        System.arraycopy(fields, 0, result, 0, SIZE);
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(fields);
    }
}

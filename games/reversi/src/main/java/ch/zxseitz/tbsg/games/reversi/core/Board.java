package ch.zxseitz.tbsg.games.reversi.core;


import ch.zxseitz.tbsg.games.reversi.Reversi;

import java.util.Arrays;

public class Board {
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

    public Board() {
        fields = new int[64];
    }

    /**
     * Checks if a field coordinate is on this board
     *
     * @param x - field x coordinate
     * @param y - field y coordinate
     * @return <code>true</code> if the field coordinate is on this board,
     * or <code>false</code> otherwise
     */
    public boolean covers(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    /**
     * Gets the state of a specific field.
     *
     * @param index field index
     * @return field state
     */
    public int get(int index) {
        if (index >= 0 && index < 64) {
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

    public int[] getFields() {
        var result = new int[fields.length];
        System.arraycopy(fields, 0, result, 0, fields.length);
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(fields);
    }
}

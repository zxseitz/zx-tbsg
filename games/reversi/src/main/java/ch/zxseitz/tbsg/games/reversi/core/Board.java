package ch.zxseitz.tbsg.games.reversi.core;


public class Board {
    // field states
    public static final int FIELD_UNDEFINED = -1;
    public static final int FIELD_EMPTY = 0;
    public static final int FIELD_BLACK = 1;
    public static final int FIELD_WHITE = 2;

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
        return FIELD_UNDEFINED;
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
}

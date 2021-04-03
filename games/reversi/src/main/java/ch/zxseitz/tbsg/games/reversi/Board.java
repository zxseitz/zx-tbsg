package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.Color;

public class Board {
    private Color[][] fields;

    void init() {
        fields = new Color[8][8];

    }

    boolean contains(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    boolean set(int x, int y, Color value) {
        if (contains(x, y) && (value == Color.BLACK || value == Color.WHITE)) {
            fields[y][x] = value;
            return true;
        }
        return false;
    }

    Color get(int x, int y) {
        if (contains(x, y)) {
            return fields[y][x];
        }
        return null;
    }
}

package ch.zxseitz.tbsg.games.reversi;

public class Board {
    private int[][] fields;

    void init() {
        fields = new int[8][8];
    }

    boolean contains(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    boolean set(int x, int y, int value) {
        if (contains(x, y) && (value == 1 || value == 2)) {
            fields[y][x] = value;
            return true;
        }
        return false;
    }

    int get(int x, int y) {
        if (contains(x, y)) {
            return fields[y][x];
        }
        return -1;
    }
}

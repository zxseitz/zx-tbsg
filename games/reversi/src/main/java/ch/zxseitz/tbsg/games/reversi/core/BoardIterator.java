package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.reversi.Reversi;

import java.util.Map;

public class BoardIterator {
    private final Board board;
    private int xDirection, yDirection;
    private int x, y;

    public BoardIterator(Board board) {
        this.board = board;
    }

    public void set(int x, int y, int xDirection, int yDirection) {
        this.x = x;
        this.y = y;
        this.xDirection = xDirection;
        this.yDirection = yDirection;
    }

    public Map.Entry<Integer, Integer> next() {
        this.x += xDirection;
        this.y += yDirection;
        if (board.covers(x, y)) {
            var index = Board.getIndex(x, y);
            return Map.entry(index, board.get(index));
        }
        return Map.entry(-1, Reversi.TOKEN_UNDEFINED);
    }
}

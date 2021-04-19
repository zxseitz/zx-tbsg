package ch.zxseitz.tbsg.games.reversi;

public class Action {
    private final int index;

    public Action(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

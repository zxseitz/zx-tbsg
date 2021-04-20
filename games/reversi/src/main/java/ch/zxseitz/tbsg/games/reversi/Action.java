package ch.zxseitz.tbsg.games.reversi;

public class Action implements Comparable<Action> {
    private final int index;

    public Action(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(Action o) {
        return Integer.compare(index, o.index);
    }
}

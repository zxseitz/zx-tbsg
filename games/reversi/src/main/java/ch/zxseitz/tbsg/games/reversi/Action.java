package ch.zxseitz.tbsg.games.reversi;

public class Action implements Comparable<Action> {
    private int index;

    public Action() {}

    public Action(int index) {
        this.index = index;
    }

    public void setIndex(int index) {
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

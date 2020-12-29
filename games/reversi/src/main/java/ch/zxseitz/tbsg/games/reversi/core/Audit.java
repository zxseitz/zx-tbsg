package ch.zxseitz.tbsg.games.reversi.core;

public class Audit {
    private final int player;
    private final int field;

    public Audit(int player, int field) {
        this.player = player;
        this.field = field;
    }

    public int getPlayer() {
        return player;
    }

    public int getField() {
        return field;
    }
}

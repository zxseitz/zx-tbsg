package ch.zxseitz.tbsg.games.reversi.core;

public class Audit<P> {
    private final P player;
    private final int field;

    public Audit(P player, int field) {
        this.player = player;
        this.field = field;
    }

    public P getPlayer() {
        return player;
    }

    public int getField() {
        return field;
    }
}

package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.IClient;

public class Audit {
    private final IClient player;
    private final int field;

    public Audit(IClient client, int field) {
        this.player = client;
        this.field = field;
    }

    public IClient getPlayer() {
        return player;
    }

    public int getField() {
        return field;
    }
}

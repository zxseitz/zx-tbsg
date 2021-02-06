package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.IAudit;
import ch.zxseitz.tbsg.games.IEvent;
import ch.zxseitz.tbsg.games.IPlayer;

public class Audit implements IAudit {
    private final IPlayer player;
    private final IEvent event;

    public Audit(IPlayer client, IEvent event) {
        this.player = client;
        this.event = event;
    }

    @Override
    public IPlayer getPlayer() {
        return player;
    }

    @Override
    public IEvent getEvent() {
        return event;
    }
}

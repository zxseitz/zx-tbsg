package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.ICommand;
import ch.zxseitz.tbsg.games.IPlayer;
import ch.zxseitz.tbsg.server.websocket.Player;

public abstract class AbstractCommand implements ICommand {
    protected Player sender;

    public AbstractCommand(Player sender) {
        this.sender = sender;
    }

    @Override
    public IPlayer getSender() {
        return sender;
    }
}

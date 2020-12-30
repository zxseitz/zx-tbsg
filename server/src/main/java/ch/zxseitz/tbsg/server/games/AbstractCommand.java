package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.ICommand;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.server.websocket.Client;

public abstract class AbstractCommand implements ICommand {
    protected Client sender;

    public AbstractCommand(Client sender) {
        this.sender = sender;
    }

    @Override
    public IClient getSender() {
        return sender;
    }
}

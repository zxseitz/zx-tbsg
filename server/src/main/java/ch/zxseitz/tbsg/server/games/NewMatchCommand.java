package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.GameException;
import ch.zxseitz.tbsg.games.IMatch;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.games.SimpleCommand;

public class NewMatchCommand extends SimpleCommand {
    public NewMatchCommand(IClient sender) {
        super(sender);
    }

    @Override
    public void execute(IMatch match) throws GameException {
        super.execute(match);
    }
}

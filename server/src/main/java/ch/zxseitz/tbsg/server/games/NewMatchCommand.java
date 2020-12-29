package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.GameException;
import ch.zxseitz.tbsg.games.IMatch;
import ch.zxseitz.tbsg.games.IPlayer;
import ch.zxseitz.tbsg.games.SimpleCommand;

public class NewMatchCommand extends SimpleCommand {
    public NewMatchCommand(IPlayer sender) {
        super(sender);
    }

    @Override
    public void execute(IMatch match) throws GameException {
        super.execute(match);
    }
}

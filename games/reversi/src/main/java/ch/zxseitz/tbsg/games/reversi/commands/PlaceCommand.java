package ch.zxseitz.tbsg.games.reversi.commands;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.reversi.core.ReversiMatch;

public class PlaceCommand extends SimpleCommand {
    private final int x,y;

    public PlaceCommand(IClient sender, int x, int y) {
        super(sender);
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute(IMatch match) throws GameException {
        var reversiMatch = (ReversiMatch) match;
        var color = reversiMatch.getColor(sender);
        reversiMatch.place(color, x, y);
    }

    @Override
    public String toString() {
        return "PLACE:"+ x + "," + y;
    }
}

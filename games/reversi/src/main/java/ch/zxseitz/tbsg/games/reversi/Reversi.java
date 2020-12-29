package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.reversi.commands.PlaceCommand;
import ch.zxseitz.tbsg.games.reversi.core.Board;
import ch.zxseitz.tbsg.games.reversi.core.ReversiMatch;

import java.io.InputStream;

@TbsgGame("reversi")
public class Reversi implements IGame {
    @Override
    public ICommand parse(IPlayer sender, String prefix, String body) {
        if (prefix.equals("PLACE")) {
            try {
                var x = Integer.parseInt(body, 0, 1, 10);
                var y = Integer.parseInt(body, 2, 3, 10);
                return new PlaceCommand(sender, x, y);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        return null;
    }

    @Override
    public IMatch newMatch(String id, IPlayer... players) {
        return new ReversiMatch(id, players[0], players[1], new Board());
    }

    @Override
    public InputStream readFile(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    @TbsgWebHook(path = "index", method = TbsgWebHook.Method.GET)
    public String test(String json) {
        return "reversi test: " + json;
    }
}

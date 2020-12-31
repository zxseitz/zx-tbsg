package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.reversi.core.Board;
import ch.zxseitz.tbsg.games.reversi.core.ReversiMatch;

import java.io.InputStream;
import java.util.UUID;

@TbsgGame("reversi")
public class Reversi implements IGame {
    private static synchronized String createMatchId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public IMatch createMatch() {
        return new ReversiMatch(createMatchId(), new Board());
    }

    @Override
    public IEvent parse(IClient sender, String prefix, String body) throws EventException {
        return null;
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

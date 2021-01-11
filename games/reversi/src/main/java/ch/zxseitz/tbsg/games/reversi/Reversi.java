package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.reversi.core.Board;
import ch.zxseitz.tbsg.games.reversi.core.ReversiMatch;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@TbsgGame("reversi")
public class Reversi implements IGame {
    private static synchronized String createMatchId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public IMatch createMatch(List<IClient> clients) {
        if (clients.size() != 2) {
            throw new IllegalArgumentException("Two clients are needed to create a match, given " + clients.size());
        }
        var black = clients.get(0);
        var white = clients.get(1);
        if (black == null || white == null) {
            throw new IllegalArgumentException("Clients contains null refs");
        }
        return new ReversiMatch(createMatchId(), black, white, new Board());
    }

    @Override
    public InputStream readFile(Path path) {
//        return getClass().getClassLoader().getResourceAsStream(path.toString());
        return null;
    }

    @Override
    public Set<String> listStyles() {
        return Set.of();
    }

    @Override
    public Set<String> listScripts() {
        return Set.of();
    }

//    @TbsgWebHook(path = "index", method = TbsgWebHook.Method.GET)
//    public String test(String json) {
//        return "reversi test: " + json;
//    }
}

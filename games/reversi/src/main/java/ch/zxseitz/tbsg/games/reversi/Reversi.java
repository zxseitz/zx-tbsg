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
    public static final int CLIENT_PLACE = 2000;

    public static final int SERVER_INIT_NEXT_PLAYER = 2100;
    public static final int SERVER_INIT_NEXT_OPPONENT = 2101;
    public static final int SERVER_NEXT_PLAYER = 2110;
    public static final int SERVER_NEXT_OPPONENT = 2111;
    public static final int SERVER_VICTORY = 2120;
    public static final int SERVER_DEFEAT = 2121;
    public static final int SERVER_TIE = 2122;

    private static synchronized String createMatchId() {
        return UUID.randomUUID().toString();
    }

    public static IEvent createInitPlayerNextEvent(int color, int[] fields, int[] preview) {
        var event = new Event(Reversi.SERVER_INIT_NEXT_PLAYER);
        event.addArgument("color", color);
        event.addArgument("board", fields);
        event.addArgument("preview", preview);
        return event;
    }

    public static IEvent createInitOpponentNextEvent(int color, int[] fields) {
        var event = new Event(Reversi.SERVER_INIT_NEXT_OPPONENT);
        event.addArgument("color", color);
        event.addArgument("board", fields);
        return event;
    }

    public static IEvent createPlayerNextEvent(int source, int[] fields, int[] preview) {
        var event = new Event(Reversi.SERVER_NEXT_PLAYER);
        event.addArgument("source", source);
        event.addArgument("board", fields);
        event.addArgument("preview", preview);
        return event;
    }

    public static IEvent createOpponentNextEvent(int source, int[] fields) {
        var event = new Event(Reversi.SERVER_INIT_NEXT_OPPONENT);
        event.addArgument("source", source);
        event.addArgument("board", fields);
        return event;
    }

    public static IEvent createVictoryEvent(int source, int[] fields) {
        var event = new Event(Reversi.SERVER_VICTORY);
        event.addArgument("source", source);
        event.addArgument("board", fields);
        return event;
    }

    public static IEvent createDefeatEvent(int source, int[] fields) {
        var event = new Event(Reversi.SERVER_DEFEAT);
        event.addArgument("source", source);
        event.addArgument("board", fields);
        return event;
    }

    public static IEvent createTieEvent(int source, int[] fields) {
        var event = new Event(Reversi.SERVER_TIE);
        event.addArgument("source", source);
        event.addArgument("board", fields);
        return event;
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

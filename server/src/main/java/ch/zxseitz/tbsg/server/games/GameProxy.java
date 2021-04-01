package ch.zxseitz.tbsg.server.games;


import ch.zxseitz.tbsg.games.Color;
import ch.zxseitz.tbsg.games.IBoard;
import ch.zxseitz.tbsg.games.reversi.Board;
import ch.zxseitz.tbsg.server.websocket.ClientEventFormat;
import ch.zxseitz.tbsg.server.websocket.EventFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GameProxy {
    private final String name;
    private final Object game;
    private final Collection<Color> colors;
    private final Map<Integer, EventFormat> serverEvents;
    private final Map<Integer, ClientEventFormat> clientEvents;
    private final Class<? extends IBoard> boardClass;

    public GameProxy(String name, Object game, Collection<Color> colors,
                     Map<Integer, EventFormat> serverEvents,
                     Map<Integer, ClientEventFormat> clientEvents,
                     Class<? extends IBoard> boardClass) {
        this.name = name;
        this.game = game;
        this.colors = Collections.unmodifiableCollection(colors);
        this.serverEvents = Collections.unmodifiableMap(serverEvents);
        this.clientEvents = Collections.unmodifiableMap(clientEvents);
        this.boardClass = boardClass;
    }

    public String getName() {
        return name;
    }

    public Object getGame() {
        return game;
    }

    public IBoard createBoard() throws Exception {
        return boardClass.getConstructor().newInstance();
    }

    public Collection<Color> getColors() {
        return colors;
    }

    public Map<Integer, ClientEventFormat> getClientEvents() {
        return clientEvents;
    }

    public Map<Integer, EventFormat> getServerEvents() {
        return serverEvents;
    }
}

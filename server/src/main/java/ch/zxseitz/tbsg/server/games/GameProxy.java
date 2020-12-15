package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.IGame;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GameProxy {
    private final String name;
    private final IGame game;
    private final Map<String, Method> webhooks;

    public GameProxy(String name, IGame game) {
        this.name = name;
        this.game = game;
        this.webhooks = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public IGame getInstance() {
        return game;
    }

    public Map<String, Method> getWebhooks() {
        return webhooks;
    }
}

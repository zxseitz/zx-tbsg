package ch.zxseitz.tbsg.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GameProxy {
    private final String name;
    private final Object game;
    private final Map<String, Method> webhooks;

    public GameProxy(String name, Object game) {
        this.name = name;
        this.game = game;
        this.webhooks = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Object getGame() {
        return game;
    }

    public Map<String, Method> getWebhooks() {
        return webhooks;
    }
}

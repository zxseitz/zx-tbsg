package ch.zxseitz.tbsg.server.games;


import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.IGame;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class GameProxy {
    private final String name;
    private final Map<Integer, String> colors;
    private final Class<? extends IGame> gameClass;
    private final Class<?> actionClass;

    public GameProxy(String name, Class<? extends IGame> gameClass,
                     Class<?> actionClass, Map<Integer, String> colors) {
        this.name = name;
        this.gameClass = gameClass;
        this.actionClass = actionClass;
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public String getColor(int color) {
        return colors.get(color);
    }

    public IGame<?> createGame() throws TbsgException {
        try {
            return gameClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new TbsgException("Unable to create " + name + " game instance");
        }
    }

    public Class<?> getActionClass() {
        return actionClass;
    }
}

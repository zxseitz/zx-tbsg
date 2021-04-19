package ch.zxseitz.tbsg.server.games;


import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.Color;
import ch.zxseitz.tbsg.games.IGame;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class GameProxy {
    private final String name;
    private final Map<Integer, String> colors;
    private final Method updateMethod;
    private final Method nextMethod;
    private final Class<? extends IGame> gameClass;
    private final Class<?> actionClass;

    public GameProxy(String name, Class<? extends IGame> gameClass, Class<?> actionClass,
                     Method updateMethod, Method nextMethod, Map<Integer, String> colors) {
        this.name = name;
        this.gameClass = gameClass;
        this.actionClass = actionClass;
        this.updateMethod = updateMethod;
        this.nextMethod = nextMethod;
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public String getColor(int color) {
        return colors.get(color);
    }

    public IGame createGame() throws TbsgException {
        try {
            return gameClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new TbsgException("Unable to create " + name + " game instance");
        }
    }

    public Class<?> getActionClass() {
        return actionClass;
    }

    /**
     * Returns a list of possible client actions.
     *
     * @param game instance
     * @return list of possible client action, must be of type actionClass
     * @throws TbsgException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public Collection<Object> pollNext(IGame game) throws TbsgException {
        try {
            return (Collection<Object>) nextMethod.invoke(game);
        } catch (Exception e) {
            throw new TbsgException("Unable to perform " + name + " client update, check arguments");
        }
    }

    /**
     * Performs an update according to the client action.
     *
     * @param game instance
     * @param action client action, must be of type actionClass
     * @throws TbsgException if an error occurs
     */
    public void performUpdate(IGame game, Object action) throws TbsgException {
        try {
            updateMethod.invoke(game, action);
        } catch (Exception e) {
            throw new TbsgException("Unable to perform " + name + " client update, check arguments");
        }
    }
}

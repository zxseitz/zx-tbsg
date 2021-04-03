package ch.zxseitz.tbsg.server.games;


import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.Color;
import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.server.websocket.ArgumentFormat;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

public class GameProxy {
    private final String name;
    private final Collection<Color> colors;
    private final Method updateMethod;
    private final ArgumentFormat[] updateArguments;
    private final Class<? extends IGame> gameClass;

    public GameProxy(String name, Class<? extends IGame> gameClass,
                     Collection<Color> colors, Method updateMethod,
                     ArgumentFormat[] updateArguments) {
        this.name = name;
        this.gameClass = gameClass;
        this.colors = Collections.unmodifiableCollection(colors);
        this.updateMethod = updateMethod;
        this.updateArguments = updateArguments;
    }

    public String getName() {
        return name;
    }

    public Collection<Color> getColors() {
        return colors;
    }

    public ArgumentFormat[] getUpdateArguments() {
        return updateArguments;
    }

    public IGame createGame() throws TbsgException {
        try {
            return gameClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new TbsgException("Unable to create " + name + " game instance");
        }
    }

    public void performUpdate(IGame game, Object[] args) throws TbsgException {
        try {
            updateMethod.invoke(game, args);
        } catch (Exception e) {
            throw new TbsgException("Unable to perform " + name + " client update, check arguments");
        }
    }
}

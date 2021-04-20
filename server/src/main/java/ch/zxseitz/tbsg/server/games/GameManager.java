package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.games.annotations.ClientNext;
import ch.zxseitz.tbsg.games.annotations.ClientUpdate;
import ch.zxseitz.tbsg.games.annotations.Color;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;
import ch.zxseitz.tbsg.games.exceptions.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class GameManager {
    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    private static final String gameBasePackage = "ch.zxseitz.tbsg.games";
//    private static final String gameClassManifest = "Game-Class";

    private final Map<String, GameProxy> proxies;

    // separate method for testing
    static GameProxy createProxy(Class<? extends IGame> gameClass) throws GameException {
        var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
        if (gameAnnotation == null) {
            throw new GameException("Missing @TbsgGame annotation in game class " + gameClass.getSimpleName());
        }
        var gameName = gameAnnotation.name();
        var colors = Arrays.stream(gameAnnotation.colors())
                .collect(Collectors.toMap(Color::value, Color::name));
        if (colors.get(0) != null) {
            throw new GameException("Color " + colors.get(0)
                    + " cannot have value 0.");
        }
        var actionClass = gameAnnotation.actionClass();
        Method clientUpdate = null;
        Method clientNext = null;

        for (var method : gameClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ClientUpdate.class)) {
                if (method.getReturnType().equals(void.class)
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].equals(actionClass)) {
                    clientUpdate = method;
                } else {
                    throw new GameException("client update method " + method.getName()
                            + " has wrong signature.");
                }
            } else if (method.isAnnotationPresent(ClientNext.class)) {
                var returnType = (ParameterizedType) method.getGenericReturnType();
                if (returnType.getRawType().equals(Collection.class)
                        && returnType.getActualTypeArguments()[0].equals(actionClass)
                        && method.getParameterCount() == 0) {
                    clientNext = method;
                } else {
                    throw new GameException("client next method " + method.getName()
                            + " has wrong signature.");
                }
            }
        }
        if (clientUpdate == null) {
            throw new GameException("Game " + gameName + " has no client update method annotated with @ClientUpdate.");
        }
        if (clientNext == null) {
            throw new GameException("Game " + gameName + " has no client next method annotated with @ClientNext.");
        }
        return new GameProxy(gameName, gameClass, actionClass, clientUpdate, clientNext, colors);
    }


    public GameManager() {
        proxies = new HashMap<>(10);
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(TbsgGame.class));
        var classes = provider.findCandidateComponents(gameBasePackage);
        for (var bean : classes) {
            var className = bean.getBeanClassName();
            logger.info("scanning game class {}", className); //todo debug
            try {
                var gameClass = Class.forName(className);
                if (IGame.class.isAssignableFrom(gameClass)) {
                    throw new GameException("Missing interface IGame in game class " + gameClass.getSimpleName());
                }
                var gameProxy = createProxy((Class<? extends IGame>) gameClass);
                proxies.put(gameProxy.getName(), gameProxy);
                logger.info("registered game {}", gameProxy.getName());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    public Set<String> listGameNames() {
        return proxies.keySet();
    }

    public void foreachGame(Consumer<GameProxy> action) {
        proxies.values().forEach(action);
    }
}

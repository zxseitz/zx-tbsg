package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.games.annotations.Color;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;
import ch.zxseitz.tbsg.games.exceptions.GameException;
import ch.zxseitz.tbsg.server.websocket.Client;
import ch.zxseitz.tbsg.server.websocket.Match;
import ch.zxseitz.tbsg.server.websocket.Protector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

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
    public static GameProxy createProxy(Class<?> gameClass) throws GameException {
        var interfaceType = Arrays.stream(gameClass.getGenericInterfaces())
                .map(type -> (ParameterizedType) type)
                .filter(type -> type.getRawType().equals(IGame.class))
                .findFirst().orElseThrow(() -> new GameException("Missing interface IGame", null));
        var actionClass = (Class<?>) interfaceType.getActualTypeArguments()[0];
        var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
        if (gameAnnotation == null) {
            throw new GameException("Missing @TbsgGame annotation in game class " + gameClass.getSimpleName(), null);
        }
        var gameName = gameAnnotation.name();
        var colors = Arrays.stream(gameAnnotation.colors())
                .collect(Collectors.toMap(Color::value, Color::name));
        if (colors.get(0) != null) {
            throw new GameException("Color " + colors.get(0) + " cannot have value 0.", null);
        }
        return new GameProxy(gameName, (Class<? extends IGame<?>>) gameClass, actionClass, colors);
    }

    public static Match createMatch(IGame<?> game, Map<Integer, Client> clients) {
        return new Match(new Protector<>(game), clients);
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
                var gameProxy = createProxy(gameClass);
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

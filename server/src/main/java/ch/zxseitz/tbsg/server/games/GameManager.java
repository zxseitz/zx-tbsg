package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.games.TbsgGame;
import ch.zxseitz.tbsg.games.TbsgWebHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class GameManager {
    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    private static final String gameBasePackage = "ch.zxseitz.tbsg.games";
    private static final String gameClassManifest = "Game-Class";

    private final Map<String, GameProxy> proxies;

    public GameManager(@Value("${app.games}") String gameDir) {
        proxies = new HashMap<>(1);

        var gamesPath = Paths.get(gameDir);
        try (var gameStream = Files.list(gamesPath)) {
            gameStream.forEach(gamePath -> {
                logger.info("Scanning jar file: {}", gamePath); //todo debug
                try {
                    var jar = new JarLoader(gamePath, Thread.currentThread().getContextClassLoader());
                    var manifest = jar.getManifest();
                    var gameClassName = manifest.getMainAttributes().getValue(gameClassManifest);
                    if (gameClassName != null) {
                        // load game class defined in manifest
                        logger.info("Scanning game class defined in manifest: {}", gameClassName);
                        var gameClass = jar.findClass(gameClassName);
                        inspectGameClass(gameClass);
                    } else {
                        // search game class
                        logger.info("Game class is not defined in manifest. Searching game class in jar file");
                        jar.loadEachClass(clazz -> {
                            logger.info("Scanning game class: {}", clazz);
                            try {
                                inspectGameClass(clazz);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        }
    }

    private void inspectGameClass(Class<?> gameClass) throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        if (IGame.class.isAssignableFrom(gameClass)
                && gameClass.isAnnotationPresent(TbsgGame.class)) {
            var gameInstance = (IGame) gameClass.getConstructor().newInstance();
            var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
            var gameName = gameAnnotation.value();
            var gameProxy = new GameProxy(gameName, gameInstance);
            for (var method : gameClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(TbsgWebHook.class)) {
                    var annotation = method.getAnnotation(TbsgWebHook.class);
                    // todo improve request method handling
                    var requestMethod = annotation.method().name();
                    gameProxy.getWebhooks().put(requestMethod + ":" + annotation.path(), method);
                    logger.info("Registered new webhook {}:{} for game {}", requestMethod, annotation.path(), gameName);
                }
            }
            proxies.put(gameName, gameProxy);
            logger.info("Registered game {}", gameName);
        }
    }

    public Set<String> listGameNames() {
        return proxies.keySet();
    }

    public GameProxy getGame(String name) {
        return proxies.get(name);
    }

    public void foreachGame(Consumer<GameProxy> action) {
        proxies.values().forEach(action);
    }
}

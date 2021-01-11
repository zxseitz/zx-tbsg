package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.games.TbsgGame;
import ch.zxseitz.tbsg.games.TbsgWebHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

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

    public GameManager() {
        proxies = new HashMap<>(10);
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(TbsgGame.class));
        provider.addIncludeFilter(new AssignableTypeFilter(IGame.class));
        var classes = provider.findCandidateComponents(gameBasePackage);
        for (var bean : classes) {
            var className = bean.getBeanClassName();
            logger.info("scanning game class {}", className); //todo debug
            try {
                var gameClass = Class.forName(className);
                var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
                var gameName = gameAnnotation.value();
                var gameInstance = (IGame) gameClass.getConstructor().newInstance();
                var gameProxy = new GameProxy(gameName, gameInstance);
                for (var method : gameClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(TbsgWebHook.class)) {
                        var wbAnnotation = method.getAnnotation(TbsgWebHook.class);
                        // todo improve request method handling
                        var webhook = wbAnnotation.method().name();
                        gameProxy.getWebhooks().put(webhook + ":" + wbAnnotation.path(), method);
                        logger.info("registered game {} webhook {}:{}", gameName, webhook, wbAnnotation.path());
                    }
                }
                proxies.put(gameName, gameProxy);
                logger.info("registered game {}", gameName);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
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

package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.TbsgException;
import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.games.annotations.UpdateArg;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;
import ch.zxseitz.tbsg.server.websocket.ArgumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

@Component
public class GameManager {
    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);
    private static final String gameBasePackage = "ch.zxseitz.tbsg.games";
//    private static final String gameClassManifest = "Game-Class";

    private final Map<String, GameProxy> proxies;

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
                if (!IGame.class.isAssignableFrom(gameClass)) {
                    throw new TbsgException("Game class " + gameClass.getSimpleName() + " has to implement interface IGame.");
                }
                var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
                var gameName = gameAnnotation.name();
                var colors = gameAnnotation.colors();
                var optionalClientUpdate = Arrays.stream(gameClass.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(ch.zxseitz.tbsg.games.annotations.ClientUpdate.class))
                        .map(method -> {
                            var params = method.getParameters();
                            var eventFormat = Map.entry(method, Arrays.stream(params)
                                    .map(param -> new ArgumentFormat(param.isAnnotationPresent(UpdateArg.class)
                                            ? param.getAnnotation(UpdateArg.class).value() : param.getName(), param.getType()))
                                    .toArray(ArgumentFormat[]::new));
                            logger.info("registered client update method " + method.getName());
                            return eventFormat;
                        }).findFirst();
                if (optionalClientUpdate.isEmpty()) {
                    throw new TbsgException("Game " + gameName + " has no client update method annotated with @ClientUpdate.");
                }
                var clientUpdate = optionalClientUpdate.get();
                @SuppressWarnings("unchecked")
                var gameProxy = new GameProxy(gameName, (Class<? extends IGame>) gameClass,
                        Arrays.asList(colors), clientUpdate.getKey(), clientUpdate.getValue());
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

    public void foreachGame(Consumer<GameProxy> action) {
        proxies.values().forEach(action);
    }
}

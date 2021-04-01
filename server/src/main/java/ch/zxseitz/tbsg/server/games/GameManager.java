package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.IBoard;
import ch.zxseitz.tbsg.games.annotations.ClientEvent;
import ch.zxseitz.tbsg.games.annotations.EventArg;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;
import ch.zxseitz.tbsg.games.reversi.Board;
import ch.zxseitz.tbsg.server.websocket.ArgumentFormat;
import ch.zxseitz.tbsg.server.websocket.ClientEventFormat;
import ch.zxseitz.tbsg.server.websocket.EventFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
                var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
                var gameName = gameAnnotation.name();
                var colors = gameAnnotation.colors();
                var serverEvents = Arrays.stream(gameAnnotation.serverEvents())
                        .map(serverEvent -> {
                            var eventFormat = new EventFormat(serverEvent.code(), Arrays.stream(serverEvent.args())
                                    // todo fix class
                                    .map(arg -> new ArgumentFormat(arg, String.class))
                                    .toArray(ArgumentFormat[]::new));
                            logger.info("registered server event (" + serverEvent.code() + ")");
                            return eventFormat;
                        })
                        .collect(Collectors.toMap(EventFormat::getCode, ef -> ef));
                var clientEvents = Arrays.stream(gameClass.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(ClientEvent.class))
                        .map(method -> {
                            var clientEvent = method.getAnnotation(ClientEvent.class);
                            var eventCode = clientEvent.value();
                            var params = method.getParameters();
                            if (!IBoard.class.isAssignableFrom(params[0].getType())) {
                                throw new RuntimeException("First param " + params[0].getName() + " in method"
                                        + method.getName() + " must implement IBoard");
                            }
                            var eventFormat = new ClientEventFormat(eventCode, Arrays.stream(params)
                                    .skip(1)
                                    .map(param -> new ArgumentFormat(param.isAnnotationPresent(EventArg.class)
                                            ? param.getAnnotation(EventArg.class).value() : param.getName(), param.getType()))
                                    .toArray(ArgumentFormat[]::new), method);
                            logger.info("registered client event (" + eventCode + ")");
                            return eventFormat;
                        }).collect(Collectors.toMap(ClientEventFormat::getCode, ef -> ef));
                var instance = gameClass.getConstructor().newInstance();
                // fixme board class
                var gameProxy = new GameProxy(gameName, instance, Arrays.asList(colors), serverEvents, clientEvents, Board.class);
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

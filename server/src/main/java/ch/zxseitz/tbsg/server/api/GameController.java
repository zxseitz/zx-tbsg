package ch.zxseitz.tbsg.server.api;

import ch.zxseitz.tbsg.server.games.GameLoader;
import ch.zxseitz.tbsg.server.games.GameProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/api/v1/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private static final String gameBasePackage = "ch.zxseitz.tbsg.games";

    private final Map<String, GameProxy> proxies;

    public GameController(@Value("${app.games}") String gameDir) {
        proxies = new HashMap<>(1);

        var gamesPath = Paths.get(gameDir);
        try (var gameStream = Files.list(gamesPath)) {
            gameStream.forEach(gamePath -> {
                logger.info("scanning jar file: {}", gamePath); //todo debug
                try {
                    var gameLoader = new GameLoader(gamePath);
                    var gameClass = gameLoader.loadClass(gameBasePackage + ".reversi.Reversi");
                    logger.info("scanning game class {}", gameClass.getName()); //todo debug
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

//        var provider = new ClassPathScanningCandidateComponentProvider(false);
//        provider.addIncludeFilter(new AnnotationTypeFilter(TbsgGame.class));
//        var classes = provider.findCandidateComponents(gameBasePackage);
//        for (var bean : classes) {
//            var className = bean.getBeanClassName();
//            logger.info("scanning game class {}", className); //todo debug
//            try {
//                var gameClass = Class.forName(className);
//                if (IGame.class.isAssignableFrom(gameClass)) {
//                    var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
//                    var gameName = gameAnnotation.value();
//                    var gameInstance = (IGame) gameClass.getConstructor().newInstance();
//                    var gameProxy = new GameProxy(gameName, gameInstance);
//                    for (var method : gameClass.getDeclaredMethods()) {
//                        if (method.isAnnotationPresent(TbsgWebHook.class)) {
//                            var annotation = method.getAnnotation(TbsgWebHook.class);
//                            // todo improve request method handling
//                            var requestMethod = annotation.method().name();
//                            gameProxy.getWebhooks().put(requestMethod + ":" + annotation.path(), method);
//                            logger.info("registered game {} webhook {}:{}", gameName, requestMethod, annotation.path());
//                        }
//                    }
//                    proxies.put(gameName, gameProxy);
//                }
//            } catch (Exception e) {
//                logger.error(e.getMessage());
//            }
//        }
    }

    @GetMapping()
    public ResponseEntity<Set<String>> games() {
        return ResponseEntity.status(200).body(proxies.keySet());
    }

    @GetMapping(
            value = "/files/{game}/{path}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<InputStreamResource> file(@PathVariable("game") String name, @PathVariable("path") String path) {
        try {
            var gameProxy = proxies.get(name);
            if (gameProxy != null) {
                var inputStream = gameProxy.getInstance().readFile(path);
                var resource = new InputStreamResource(inputStream);
                return ResponseEntity.status(200).body(resource);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    @RequestMapping(path = "/webhooks/{game}/{webhook}")
    public ResponseEntity<?> webhook(@PathVariable("game") String name, @PathVariable("webhook") String path,
                                     final HttpServletRequest request, @RequestBody(required = false) String json) {
        var requestMethod = request.getMethod();
        try {
            var gameProxy = proxies.get(name);
            if (gameProxy != null) {
                var method = gameProxy.getWebhooks().get(requestMethod + ":" + path);
                if (method != null) {
                    var result = method.invoke(gameProxy.getInstance(), json);
                    return ResponseEntity.status(200).body(result);
                }
            }
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.status(500).build();
    }
}

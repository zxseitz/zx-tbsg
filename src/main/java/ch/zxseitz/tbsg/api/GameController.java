package ch.zxseitz.tbsg.api;

import ch.zxseitz.tbsg.games.TbsgGame;
import ch.zxseitz.tbsg.games.TbsgWebHook;

import ch.zxseitz.tbsg.model.GameProxy;
import ch.zxseitz.tbsg.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private static final String gameBasePackage = "ch.zxseitz.tbsg.games";

    private final Map<String, GameProxy> proxies;

    public GameController() {
        proxies = new HashMap<>(1);

        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(TbsgGame.class));
        var classes = provider.findCandidateComponents(gameBasePackage);
        for (var bean : classes) {
            var className = bean.getBeanClassName();
            logger.info("scanning game class {}", className); //todo debug
            try {
                var gameClass = Class.forName(className);
                var gameAnnotation = gameClass.getAnnotation(TbsgGame.class);
                var gameName = gameAnnotation.value();
                var gameInstance = gameClass.getConstructor().newInstance();
                var gameProxy = new GameProxy(gameName, gameInstance);
                for (var method : gameClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(TbsgWebHook.class)) {
                        var annotation = method.getAnnotation(TbsgWebHook.class);
                        // todo improve request method handling
                        var requestMethod = annotation.method().name();
                        gameProxy.getWebhooks().put(requestMethod + ":" + annotation.path(), method);
                        logger.info("registered game {} webhook {}:{}", gameName, requestMethod, annotation.path());
                    }
                }
                proxies.put(gameName, gameProxy);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    @GetMapping()
    public ResponseEntity<Set<String>> games() {
        return ResponseEntity.status(200).body(proxies.keySet());
    }

    @RequestMapping(path = "/{game}/{webhook}")
    public ResponseEntity<?> webhook(@PathVariable("game") String name, @PathVariable("webhook") String path,
                                     final HttpServletRequest request, @RequestBody(required = false) String json) {
        var requestMethod = request.getMethod();
        try {
            var gameProxy = proxies.get(name);
            if (gameProxy != null) {
                var method = gameProxy.getWebhooks().get(requestMethod + ":" + path);
                if (method != null) {
                    var result = method.invoke(gameProxy.getGame(), json);
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

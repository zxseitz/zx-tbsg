package ch.zxseitz.tbsg.server.api;

import ch.zxseitz.tbsg.server.games.GameManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/api/v1/games")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final GameManager gameManager;

    public GameController(@Autowired GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @GetMapping()
    public ResponseEntity<Set<String>> games() {
        return ResponseEntity.status(200).body(gameManager.listGameNames());
    }

    @GetMapping(
            value = "/{game}/files/{path}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<InputStreamResource> file(@PathVariable("game") String name, @PathVariable("path") String path) {
        try {
            var gameProxy = gameManager.getGame(name);
            if (gameProxy != null) {
                var inputStream = gameProxy.getInstance().readFile(path);
                var resource = new InputStreamResource(inputStream);
                return ResponseEntity.status(200).body(resource);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.status(400).build();
    }

    @RequestMapping(path = "/{game}/webhooks/{webhook}")
    public ResponseEntity<?> webhook(@PathVariable("game") String name, @PathVariable("webhook") String path,
                                     final HttpServletRequest request, @RequestBody(required = false) String json) {
        var requestMethod = request.getMethod();
        try {
            var gameProxy = gameManager.getGame(name);
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

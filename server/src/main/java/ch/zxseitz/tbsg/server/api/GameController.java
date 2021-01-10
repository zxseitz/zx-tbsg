package ch.zxseitz.tbsg.server.api;

import ch.zxseitz.tbsg.server.games.GameManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @GetMapping(value = "/{game}/styles")
    public ResponseEntity<Set<String>> styles(@PathVariable("game") String name) {
        try {
            var gameProxy = gameManager.getGame(name);
            if (gameProxy != null) {
                return ResponseEntity.status(200).body(gameProxy.getInstance().listStyles());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.status(400).build();
    }

    @GetMapping(value = "/{game}/scripts")
    public ResponseEntity<Set<String>> scripts(@PathVariable("game") String name) {
        try {
            var gameProxy = gameManager.getGame(name);
            if (gameProxy != null) {
                return ResponseEntity.status(200).body(gameProxy.getInstance().listScripts());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.status(400).build();
    }

    @GetMapping(value = "/{game}/files/{path}")
    public ResponseEntity<Resource> file(@PathVariable("game") String name, @PathVariable("path") String path) {
        try {
            var gameProxy = gameManager.getGame(name);
            if (gameProxy != null) {
                var localPath = Paths.get(path);
                var type = MediaType.parseMediaType(Files.probeContentType(localPath));
                var inputStream = gameProxy.getInstance().readFile(localPath);
                var resource = new InputStreamResource(inputStream);
                var downloadName = name + "_" + path.replace('/', '.');
                return ResponseEntity.status(200)
                        .headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=" + downloadName))
                        .contentType(type)
                        .body(resource);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.status(400).build();
    }

    @GetMapping(
            value = "/{game}/blob/{path}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<Resource> blob(@PathVariable("game") String name, @PathVariable("path") String path) {
        try {
            var gameProxy = gameManager.getGame(name);
            if (gameProxy != null) {
                var localPath = Paths.get(path);
                var inputStream = gameProxy.getInstance().readFile(localPath);
                var resource = new InputStreamResource(inputStream);
                return ResponseEntity.status(200)
                        .body(resource);
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

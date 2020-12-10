package ch.zxseitz.tbsg.api;

import ch.zxseitz.tbsg.games.reversi.Reversi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/games")
public class GameController {
    private final Reversi reversi;

    public GameController() {
        reversi = new Reversi();
    }

    @GetMapping(path = "/reversi")
    public ResponseEntity<String> home() {
        return ResponseEntity.status(200).body(reversi.test());
    }
}

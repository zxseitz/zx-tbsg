package ch.zxseitz.tbsg.server.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/v1/test")
class TestController {
    @GetMapping("/hello")
    fun home(): ResponseEntity<String> {
        return ResponseEntity.status(200).body("welcome");
    }
}

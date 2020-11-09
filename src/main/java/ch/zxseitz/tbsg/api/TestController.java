package ch.zxseitz.tbsg.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    @GetMapping(path = "/hello")
    public String home() {
        return ("<h1>Welcome</h1>");
    }
}

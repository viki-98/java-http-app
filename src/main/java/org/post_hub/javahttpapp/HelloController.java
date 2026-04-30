package org.post_hub.javahttpapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "message", "Spring Boot app is running",
                "message", "testing jenkins",
                "endpoint", "/"
        );
    }

    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello from Spring Boot",
                "endpoint", "/api/hello"
        );
    }
}
package ru.nsu.crackhash.worker.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/worker/health")
    public Map<String, String> health() {
        String pod = System.getenv("HOSTNAME");
        return Map.of(
                "status", "UP",
                "pod", pod == null ? "unknown" : pod);
    }
}

package ru.nsu.crackhash.manager.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class SprintHealthController {

    @GetMapping("/sprint1/health")
    public Map<String, String> health() {
        log.info("Get health probe");
        String pod = System.getenv("HOSTNAME");
        return Map.of(
                "status", "UP",
                "pod", pod == null ? "unknown" : pod);
    }
}

package ru.nsu.crackhash.worker.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;

@RequiredArgsConstructor
@RequestMapping("/internal/api/worker")
@RestController
public class CrackHashTaskController {

    private final HashCrackingService hashCrackingService;

    @PostMapping("/hash/crack/task")
    public void createCrackHashTask(
        @RequestBody CreateCrackHashTaskRequest createCrackHashTaskRequest
    ) {
        hashCrackingService.createCrackHashTask(createCrackHashTaskRequest);
    }
}

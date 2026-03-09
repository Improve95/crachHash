package ru.nsu.crackhash.manager.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.crackhash.manager.api.dto.GetCrackHashProcessStatusResponse;
import ru.nsu.crackhash.manager.api.dto.ReceiveCrackResultRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.core.service.HashWordService;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/hash")
@RestController
public class CrackHashController {

    private final HashWordService hashWordService;

    @PostMapping("/crack")
    public ResponseEntity<StartCrackingHashProcessResponse> findWord(
        @RequestBody StartCrackingHashProcessRequest startCrackingHashProcessRequest
    ) {
        var response = hashWordService.startCrackHash(startCrackingHashProcessRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/task/result")
    public void receiveResult(@RequestBody ReceiveCrackResultRequest request) {
        hashWordService.receiveCrackHashResult(request);
    }

    @PostMapping("/request/{id}/status")
    public ResponseEntity<GetCrackHashProcessStatusResponse> getWordStatusResponse(@PathVariable("id") UUID requestId) {
        return null;
    }
}

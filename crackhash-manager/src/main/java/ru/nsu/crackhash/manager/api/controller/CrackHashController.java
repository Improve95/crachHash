package ru.nsu.crackhash.manager.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.crackhash.manager.api.dto.GetCrackHashProcessStatusResponse;
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

    @PostMapping("/status")
    public ResponseEntity<GetCrackHashProcessStatusResponse> getWordStatusResponse(
        @RequestParam("requestId") UUID requestId
    ) {
        return null;
    }
}

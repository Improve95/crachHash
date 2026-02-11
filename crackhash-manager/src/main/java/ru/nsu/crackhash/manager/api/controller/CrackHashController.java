package ru.nsu.crackhash.manager.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.crackhash.manager.api.dto.GetCrackHashProcessStatusResponse;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/hash")
@RestController
public class CrackHashController {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/crack")
    public StartCrackingHashProcessResponse findWord(@RequestBody StartCrackingHashProcessRequest startCrackingHashProcessRequest) {
        return null;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/status")
    public GetCrackHashProcessStatusResponse getWordStatusResponse(@RequestParam("requestId") UUID requestId) {
        return null;
    }
}

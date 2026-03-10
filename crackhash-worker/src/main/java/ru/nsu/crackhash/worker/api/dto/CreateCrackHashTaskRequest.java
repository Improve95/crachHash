package ru.nsu.crackhash.worker.api.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CreateCrackHashTaskRequest(
        String hash,
        List<String> alphabet,
        int maxLength,
        int partNumber,
        long partCount,
        UUID requestId
) {}

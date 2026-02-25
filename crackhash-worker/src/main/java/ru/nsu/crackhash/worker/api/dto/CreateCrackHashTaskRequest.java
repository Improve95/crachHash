package ru.nsu.crackhash.worker.api.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateCrackHashTaskRequest(
        String hash,
        char[] alphabet,
        int maxLength,
        int partNumber,
        long partCount,
        UUID requestId
) {}

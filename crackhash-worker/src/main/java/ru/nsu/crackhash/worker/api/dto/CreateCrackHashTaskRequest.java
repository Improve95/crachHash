package ru.nsu.crackhash.worker.api.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateCrackHashTaskRequest(
        String hash,
        int maxLength,
        char[] alphabet,
        int partNumber,
        int partCount,
        UUID requestId
) {}

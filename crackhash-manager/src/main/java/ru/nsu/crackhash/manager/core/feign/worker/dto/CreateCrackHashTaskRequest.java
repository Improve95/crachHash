package ru.nsu.crackhash.manager.core.feign.worker.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateCrackHashTaskRequest(
    UUID requestId,
    int partNumber,
    long partCount,
    String hash,
    int maxLength,
    char[] alphabet
) {}

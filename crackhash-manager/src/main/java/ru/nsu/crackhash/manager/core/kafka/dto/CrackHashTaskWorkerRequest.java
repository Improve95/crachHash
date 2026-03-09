package ru.nsu.crackhash.manager.core.kafka.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CrackHashTaskWorkerRequest(
    UUID requestId,
    int partNumber,
    long partCount,
    String hash,
    int maxLength,
    char[] alphabet
) {}

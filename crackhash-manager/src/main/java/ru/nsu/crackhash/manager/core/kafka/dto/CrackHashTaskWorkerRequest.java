package ru.nsu.crackhash.manager.core.kafka.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CrackHashTaskWorkerRequest(
    String hash,
    List<String> alphabet,
    int maxLength,
    int partNumber,
    long partCount,
    UUID requestId
) {}

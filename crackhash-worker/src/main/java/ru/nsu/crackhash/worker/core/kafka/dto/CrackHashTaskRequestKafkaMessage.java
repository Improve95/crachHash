package ru.nsu.crackhash.worker.core.kafka.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CrackHashTaskRequestKafkaMessage(
    String hash,
    char[] alphabet,
    int maxLength,
    int partNumber,
    long partCount,
    UUID requestId
) {}

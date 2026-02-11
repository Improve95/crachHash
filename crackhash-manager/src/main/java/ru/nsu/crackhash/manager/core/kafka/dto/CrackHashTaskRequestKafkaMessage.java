package ru.nsu.crackhash.manager.core.kafka.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CrackHashTaskRequestKafkaMessage(
    UUID requestId,
    int partNumber,
    long partCount,
    String hash,
    int maxLength,
    char[] alphabet
) {}

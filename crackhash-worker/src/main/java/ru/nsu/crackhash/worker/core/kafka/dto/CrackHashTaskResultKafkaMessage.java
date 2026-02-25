package ru.nsu.crackhash.worker.core.kafka.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CrackHashTaskResultKafkaMessage(
    UUID requestId,
    int partNumber,
    List<String> answers
) {}

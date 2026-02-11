package ru.nsu.crackhash.manager.core.kafka.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CrackHashTaskResultKafkaMessage(
    String requestId,
    int partNumber,
    List<String> answers
) {}

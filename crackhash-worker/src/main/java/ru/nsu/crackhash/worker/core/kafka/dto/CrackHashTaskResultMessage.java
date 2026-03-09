package ru.nsu.crackhash.worker.core.kafka.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CrackHashTaskResultMessage(
    UUID taskId,
    List<String> answers
) {}

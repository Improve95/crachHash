package ru.nsu.crackhash.worker.core.feign.manager.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record SendCrackResultRequest(UUID taskId, List<String> answers) {}
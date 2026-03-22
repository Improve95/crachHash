package ru.nsu.crackhash.worker.core.feign.manager.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AddWorkerTaskProgressRequest(UUID taskId, int increaseProgressPercent) {}

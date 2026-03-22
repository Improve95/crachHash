package ru.nsu.crackhash.worker.core.service.impl;

import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;

import java.util.UUID;

public interface ResultService {
    void sendTaskResultToManager(SendCrackResultRequest sendCrackResultRequest);

    boolean updateTaskProgress(long checkedWords, long diffChangeSizeForRequiredPercent, UUID taskId);
}

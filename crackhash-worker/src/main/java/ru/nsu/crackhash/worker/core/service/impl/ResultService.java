package ru.nsu.crackhash.worker.core.service.impl;

import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;

public interface ResultService {
    void sendTaskResultToManager(SendCrackResultRequest sendCrackResultRequest);
}

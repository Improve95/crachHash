package ru.nsu.crackhash.worker.core.service;

import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;

public interface ResultService {

    void sendResultToManager(SendCrackResultRequest crackHashTaskResultMessage);
}

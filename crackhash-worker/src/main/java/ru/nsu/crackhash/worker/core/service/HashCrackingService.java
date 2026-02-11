package ru.nsu.crackhash.worker.core.service;

import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;

public interface HashCrackingService {

    void createCrackHashTask(CreateCrackHashTaskRequest createCrackHashTaskRequest);
}

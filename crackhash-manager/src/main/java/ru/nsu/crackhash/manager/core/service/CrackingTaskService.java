package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;

import java.util.List;
import java.util.UUID;

public interface CrackingTaskService {

    List<CrackHashTaskWorkerRequest> createCrackRequest(
        UUID requestId, StartCrackingHashProcessRequest request
    );

    int calculateNewTaskProgress(int increasePercent, CrackingHashTask task);
}

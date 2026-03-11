package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;

import java.util.List;
import java.util.UUID;

public interface CrackingTaskService {

    List<CrackHashTaskWorkerRequest> createCrackRequest(
        UUID requestId, StartCrackingHashProcessRequest request
    );
}

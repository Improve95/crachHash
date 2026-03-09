package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;

import java.util.List;

public interface CrackHashTaskDistributed {

    void distributedSendCrackHashTasks(List<CrackHashTaskWorkerRequest> requests);
}

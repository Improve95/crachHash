package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;

public interface CrackHashTaskDistributed {

    void distributedSendCrackHashTasks(CrackHashTaskWorkerRequest request);
}

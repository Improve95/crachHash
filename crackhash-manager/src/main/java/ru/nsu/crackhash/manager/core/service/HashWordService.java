package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskResultKafkaMessage;

public interface HashWordService {
    StartCrackingHashProcessResponse startCrackHash(StartCrackingHashProcessRequest startFindWordProcessRequest);

    void handleCrackHashTaskResult(CrackHashTaskResultKafkaMessage result);
}

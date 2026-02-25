package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskResultKafkaMessage;

public interface CrackingHashResultCollector {

    void handleCrackHashTaskResult(CrackHashTaskResultKafkaMessage result);
}

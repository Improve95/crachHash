package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskRequestKafkaMessage;

import java.util.List;

public interface CrackHashTaskDistributed {
    void distributedSendCrackHashTasks(List<CrackHashTaskRequestKafkaMessage> requests);
}

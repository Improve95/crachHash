package ru.nsu.crackhash.manager.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskResultKafkaMessage;
import ru.nsu.crackhash.manager.core.service.CrackingHashResultCollector;

@Slf4j
@Service
public class CrackingHashResultCollectorImpl implements CrackingHashResultCollector {

    @Override
    public void handleCrackHashTaskResult(CrackHashTaskResultKafkaMessage result) {
        log.info("Received crack hash task result, id: {}", result.requestId());
    }
}

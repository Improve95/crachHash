package ru.nsu.crackhash.worker.core.service.impl.result;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;
import ru.nsu.crackhash.worker.core.service.ResultService;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "send-type", havingValue = "kafka")
@Service
public class KafkaResultServiceImpl implements ResultService {

    @Override
    public void sendResultToManager(SendCrackResultRequest sendCrackResultRequest) {

    }
}

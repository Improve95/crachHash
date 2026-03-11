package ru.nsu.crackhash.worker.core.service.impl.result;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.config.kafka.KafkaConfig;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;
import ru.nsu.crackhash.worker.core.kafka.CrackingHashTaskResultKafkaProducer;
import ru.nsu.crackhash.worker.core.service.ResultService;

@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Service
public class KafkaResultServiceImpl implements ResultService {

    private final CrackingHashTaskResultKafkaProducer crackingHashTaskResultKafkaProducer;

    @Value("${crack-hash.kafka.producer.topic}")
    private String crackHashResultTopic;

    @Override
    public void sendResultToManager(SendCrackResultRequest sendCrackResultRequest) {
        crackingHashTaskResultKafkaProducer.sendCrackHashTaskResult(
            crackHashResultTopic, sendCrackResultRequest
        );
    }
}

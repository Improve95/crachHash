package ru.nsu.crackhash.worker.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.worker.config.kafka.KafkaConfig;
import ru.nsu.crackhash.worker.core.kafka.dto.CrackHashTaskResultMessage;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class CrackingHashTaskResultKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void sendCrackHashTaskResult(String topic, CrackHashTaskResultMessage message) {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    }
}

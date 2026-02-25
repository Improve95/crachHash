package ru.nsu.crackhash.worker.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.worker.core.kafka.dto.CrackHashTaskResultKafkaMessage;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class CrackingHashTaskResultKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void sendCrackHashTaskResult(String topic, CrackHashTaskResultKafkaMessage message) {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    }
}

package ru.nsu.crackhash.manager.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class CrackingHashTaskRequestKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void sendCrackHashTask(String topic, CrackHashTaskWorkerRequest message) {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    }
}

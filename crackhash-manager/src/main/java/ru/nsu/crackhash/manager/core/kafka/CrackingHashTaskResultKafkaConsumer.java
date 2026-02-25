package ru.nsu.crackhash.manager.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskResultKafkaMessage;
import ru.nsu.crackhash.manager.core.service.CrackingHashResultCollector;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class CrackingHashTaskResultKafkaConsumer {

    private final CrackingHashResultCollector crackingHashResultCollector;

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${crack-hash.kafka.consumer.topic}",
        containerFactory = "crackHashConsumerFactory"
    )
    private void listenCrackHashTaskResultTopic(String body, Acknowledgment acknowledgment) {
        var crackingHashResult = objectMapper.readValue(body, CrackHashTaskResultKafkaMessage.class);
        crackingHashResultCollector.handleCrackHashTaskResult(crackingHashResult);
        acknowledgment.acknowledge();
    }
}

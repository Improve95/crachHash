package ru.nsu.crackhash.manager.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskResultKafkaMessage;
import ru.nsu.crackhash.manager.core.service.HashWordService;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class CrackingHashTaskResultKafkaConsumer {

    private final HashWordService hashWordService;

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${crack-hash.kafka.consumer.topic}",
        containerFactory = "crackHashConsumerFactory"
    )
    private void listenCrackHashTaskResultTopic(String body, Acknowledgment acknowledgment) {
        var crackingHashResult = objectMapper.readValue(body, CrackHashTaskResultKafkaMessage.class);
        hashWordService.handleCrackHashTaskResult(crackingHashResult);
        acknowledgment.acknowledge();
    }
}

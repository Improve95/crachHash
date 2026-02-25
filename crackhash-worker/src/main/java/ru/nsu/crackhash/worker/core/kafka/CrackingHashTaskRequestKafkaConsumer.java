package ru.nsu.crackhash.worker.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.worker.core.kafka.dto.CrackHashTaskRequestKafkaMessage;
import ru.nsu.crackhash.worker.core.mapper.CrackHashRequestDtoMapper;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class CrackingHashTaskRequestKafkaConsumer {

    private final HashCrackingService hashCrackingService;

    private final CrackHashRequestDtoMapper crackHashRequestDtoMapper;

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${crack-hash.kafka.consumer.topic}",
        containerFactory = "crackHashConsumerFactory"
    )
    private void listenCrackHashTaskResultTopic(String body, Acknowledgment acknowledgment) {
        var crackingHashRequestMessage = objectMapper.readValue(
            body, CrackHashTaskRequestKafkaMessage.class
        );

        var isComplete = hashCrackingService.createCrackHashTask(
            crackHashRequestDtoMapper.toCreateCrackHashTaskRequest(crackingHashRequestMessage)
        );

        if (isComplete) {
            acknowledgment.acknowledge();
        }
    }
}

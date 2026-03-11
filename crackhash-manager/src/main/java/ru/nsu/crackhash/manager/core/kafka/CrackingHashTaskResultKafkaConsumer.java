package ru.nsu.crackhash.manager.core.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.api.dto.ReceiveCrackResultRequest;
import ru.nsu.crackhash.manager.config.kafka.KafkaConfig;
import ru.nsu.crackhash.manager.core.service.HashWordService;
import tools.jackson.databind.ObjectMapper;

@ConditionalOnBean(KafkaConfig.class)
@Component
public class CrackingHashTaskResultKafkaConsumer {

    private final HashWordService hashWordService;

    private final ObjectMapper objectMapper;

    public CrackingHashTaskResultKafkaConsumer(
        HashWordService hashWordService,
        ObjectMapper objectMapper
    ) {
        this.hashWordService = hashWordService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${crack-hash.kafka.consumer.topic}",
        containerFactory = "crackHashTaskResultKafkaListenerContainerFactory"
    )
    private void listenCrackHashTaskResultTopic(String body) {
        hashWordService.receiveCrackHashResult(
            objectMapper.readValue(body, ReceiveCrackResultRequest.class)
        );
    }
}

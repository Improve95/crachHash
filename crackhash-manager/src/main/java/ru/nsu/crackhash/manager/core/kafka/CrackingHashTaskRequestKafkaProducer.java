package ru.nsu.crackhash.manager.core.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.config.kafka.KafkaConfig;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class CrackingHashTaskRequestKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void sendCrackHashTask(String topic, CrackHashTaskWorkerRequest message) {
        CompletableFuture<SendResult<String, String>> sendResultCompletableFuture = kafkaTemplate.send(
            topic, objectMapper.writeValueAsString(message)
        );

        sendResultCompletableFuture.thenAcceptAsync(sendResult ->
            log.info(
                "success sending crack hash task request to topic: {}, paritition: {}, requestId: {}, partNumber: {}",
                topic,
                sendResult.getRecordMetadata().partition(),
                message.requestId(),
                message.partNumber()
            )
        );
    }
}

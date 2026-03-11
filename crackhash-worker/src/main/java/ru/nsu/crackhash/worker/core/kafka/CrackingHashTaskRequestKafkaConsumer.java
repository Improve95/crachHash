package ru.nsu.crackhash.worker.core.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.worker.config.kafka.KafkaConfig;
import ru.nsu.crackhash.worker.core.kafka.dto.CrackHashTaskRequestKafkaMessage;
import ru.nsu.crackhash.worker.core.mapper.CrackHashRequestDtoMapper;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class CrackingHashTaskRequestKafkaConsumer {

    private final HashCrackingService hashCrackingService;

    private final CrackHashRequestDtoMapper crackHashRequestDtoMapper;

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${crack-hash.kafka.consumer.topic}",
        containerFactory = "crackHashTaskRequestKafkaListenerContainerFactory",
        concurrency = "${crack-hash.kafka.consumer.properties.concurrency}"
    )
    private void listenCrackHashTaskResultTopic(
        ConsumerRecord<String, String> consumerRecord,
        Acknowledgment ack
    ) {
        var crackingHashRequestMessage = objectMapper.readValue(
            consumerRecord.value(), CrackHashTaskRequestKafkaMessage.class
        );

        var isCompleteFuture = hashCrackingService.createCrackHashTask(
            crackHashRequestDtoMapper.toCreateCrackHashTaskRequest(crackingHashRequestMessage)
        );

        boolean isComplete = true;
        try {
            isComplete = isCompleteFuture.get(5000, TimeUnit.MILLISECONDS);
            log.info(
                "success cracking hash with requestId: {}, key: {}, topic: {}, partition: {}, offset: {}",
                crackingHashRequestMessage.requestId(),
                consumerRecord.key(),
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset()
            );
        } catch (Exception ex) {
            log.error(
                "failed cracking hash with requestId: {}, key: {}, topic: {}, partition: {}, offset: {}, cause: {}",
                crackingHashRequestMessage.requestId(),
                consumerRecord.key(),
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                ExceptionUtils.getRootCauseMessage(ex)
            );
        }

        if (isComplete) {
            ack.acknowledge();
        }
    }
}

package ru.nsu.crackhash.worker.core.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;
import ru.nsu.crackhash.worker.config.kafka.KafkaConfig;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class CrackingHashTaskRequestKafkaConsumer {

    private final HashCrackingService hashCrackingService;

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${crack-hash.kafka.consumer.topic}",
        containerFactory = "crackHashTaskRequestKafkaListenerContainerFactory",
        concurrency = "${crack-hash.kafka.consumer.properties.concurrency}"
    )
    private void listenCrackHashTaskResultTopic(ConsumerRecord<String, String> consumerRecord, Acknowledgment ack) {
        var createCrackHashTaskRequest = objectMapper.readValue(
            consumerRecord.value(), CreateCrackHashTaskRequest.class
        );

        log.info(
            "success receive cracking hash task by kafka with requestId: {}, key: {}, topic: {}, partition: {}, offset: {}",
            createCrackHashTaskRequest.requestId(),
            consumerRecord.key(),
            consumerRecord.topic(),
            consumerRecord.partition(),
            consumerRecord.offset()
        );
        var isCompleteFuture = hashCrackingService.createCrackHashTask(createCrackHashTaskRequest);
        isCompleteFuture.join();
        ack.acknowledge();
    }
}

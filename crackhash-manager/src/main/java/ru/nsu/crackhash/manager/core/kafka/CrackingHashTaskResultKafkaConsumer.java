package ru.nsu.crackhash.manager.core.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.config.kafka.KafkaConfig;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskResultKafkaMessage;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class CrackingHashTaskResultKafkaConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${crack-hash.kafka.consumer.topic}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    private void listenCrackHashTaskResultTopic(String body, Acknowledgment acknowledgment) {
        var crackingHashResult = objectMapper.readValue(body, CrackHashTaskResultKafkaMessage.class);
        acknowledgment.acknowledge();
    }
}

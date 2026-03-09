package ru.nsu.crackhash.worker.config.kafka.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import ru.nsu.crackhash.worker.config.kafka.KafkaConfig;

@ConditionalOnBean(KafkaConfig.class)
@ConfigurationProperties(prefix = "crack-hash.kafka")
public record CrackHashKafkaProperties(
    CrackHashProducer producer,
    CrackHashConsumer consumer
) {
    public record CrackHashProducer(KafkaProperties.Producer config, String topic) {}
    public record CrackHashConsumer(KafkaProperties.Consumer config, String topic) {}
}

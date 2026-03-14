package ru.nsu.crackhash.worker.config.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import ru.nsu.crackhash.worker.config.kafka.properties.CrackHashKafkaProperties;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "send-type", havingValue = "kafka")
@Configuration
public class KafkaConfig {

    private final CrackHashKafkaProperties crackHashKafkaProperties;

    @Bean
    public ProducerFactory<String, String> crackHashProducerFactory() {
        var props = crackHashKafkaProperties.producer().config().buildProperties();
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> crackHashKafkaProducer(
        @Qualifier("crackHashProducerFactory") ProducerFactory<String, String> outboxProducerFactory
    ) {
        return new KafkaTemplate<>(outboxProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> crackHashConsumerFactory() {
        var props = crackHashKafkaProperties.consumer().config().buildProperties();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> crackHashTaskRequestKafkaListenerContainerFactory(
        @Qualifier("crackHashConsumerFactory") ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}

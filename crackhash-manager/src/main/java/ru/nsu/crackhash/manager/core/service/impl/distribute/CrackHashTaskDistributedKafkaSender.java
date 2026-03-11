package ru.nsu.crackhash.manager.core.service.impl.distribute;

import jakarta.websocket.SendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.config.kafka.KafkaConfig;
import ru.nsu.crackhash.manager.core.kafka.CrackingHashTaskRequestKafkaProducer;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Primary
@ConditionalOnBean(KafkaConfig.class)
@Service
public class CrackHashTaskDistributedKafkaSender implements CrackHashTaskDistributed {

    private final CrackingHashTaskRequestKafkaProducer crackingHashTaskRequestKafkaProducer;

    @Value("${crack-hash.kafka.producer.topic}")
    private String crackHashTaskRequestTopic;

    @Override
    public void distributedSendCrackHashTasks(List<CrackHashTaskWorkerRequest> requests) {
        requests.forEach(request -> crackingHashTaskRequestKafkaProducer.sendCrackHashTask(crackHashTaskRequestTopic, request));
    }
}

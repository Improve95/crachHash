package ru.nsu.crackhash.manager.core.service.impl.distribute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskRequestKafkaMessage;
import ru.nsu.crackhash.manager.core.kafka.CrackingHashTaskRequestKafkaProducer;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrackHashTaskDistributedKafkaSender implements CrackHashTaskDistributed {

    private final CrackingHashTaskRequestKafkaProducer crackingHashTaskRequestKafkaProducer;

    @Value("${crack-hash.kafka.producer.topic}")
    private String crackHashTaskRequestTopic;

    @Override
    public void distributedSendCrackHashTasks(List<CrackHashTaskRequestKafkaMessage> requests) {
        requests.forEach(request -> {
            log.info("Sending crack hash task request to topic {}: {}", crackHashTaskRequestTopic, request);
            crackingHashTaskRequestKafkaProducer.sendCrackHashTask(crackHashTaskRequestTopic, request);
        });
    }
}

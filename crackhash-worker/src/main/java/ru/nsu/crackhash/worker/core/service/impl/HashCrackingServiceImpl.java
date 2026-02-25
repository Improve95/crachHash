package ru.nsu.crackhash.worker.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;
import ru.nsu.crackhash.worker.core.kafka.CrackingHashTaskResultKafkaProducer;
import ru.nsu.crackhash.worker.core.kafka.dto.CrackHashTaskResultKafkaMessage;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;
import ru.nsu.crackhash.worker.core.service.crypto.CryptoService;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashCrackingServiceImpl implements HashCrackingService {

    private final CryptoService cryptoService;

    private final CrackingHashTaskResultKafkaProducer crackingHashTaskResultKafkaProducer;

    @Value("${crack-hash.kafka.producer.topic}")
    private String crackHashResultTopic;

    @Override
    public boolean createCrackHashTask(CreateCrackHashTaskRequest createCrackHashTaskRequest) {
        try {
            var answers = Generator.permutation(createCrackHashTaskRequest.alphabet())
                .withRepetitions(createCrackHashTaskRequest.maxLength())
                .stream()
                .skip(createCrackHashTaskRequest.partCount() * createCrackHashTaskRequest.partNumber())
                .flatMap(charsList -> charsList.stream().map(String::new))
                .map(cryptoService::hashingByMd5)
                .filter(hashingWord -> createCrackHashTaskRequest.hash().equals(hashingWord))
                .toList();

            var resultMessage = CrackHashTaskResultKafkaMessage.builder()
                .requestId(createCrackHashTaskRequest.requestId())
                .partNumber(createCrackHashTaskRequest.partNumber())
                .answers(answers)
                .build();

            crackingHashTaskResultKafkaProducer.sendCrackHashTaskResult(crackHashResultTopic, resultMessage);

            log.info("success cracking hash task, id: {}", createCrackHashTaskRequest.requestId());
            return true;
        } catch (Exception ex) {
            log.error(
                "failed cracking hash task, id: {} {}",
                createCrackHashTaskRequest.requestId(),
                ExceptionUtils.getRootCauseMessage(ex)
            );
            return false;
        }
    }
}

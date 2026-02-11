package ru.nsu.crackhash.manager.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.config.alphabet.AlphabetConfig;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskRequestKafkaMessage;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskResultKafkaMessage;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;
import ru.nsu.crackhash.manager.core.service.HashWordService;

import java.math.BigInteger;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashWordServiceImpl implements HashWordService {

    private final CrackHashTaskDistributed crackHashTaskDistributed;

    private final AlphabetConfig alphabetConfig;

    @Value("${worker.number}")
    private int workersNumber;

    @Override
    public StartCrackingHashProcessResponse startCrackHash(StartCrackingHashProcessRequest request) {
        BigInteger bigInteger = BigInteger.valueOf(alphabetConfig.getAlphabet().length);
        bigInteger = bigInteger.pow(request.maxLength());
        long potentialWordsCount = bigInteger.longValue();

        var taskRequestId = UUID.randomUUID();
        long wordsPerWorker = potentialWordsCount / workersNumber;

        var taskRequests = IntStream.range(0, workersNumber)
            .boxed()
            .map(i -> CrackHashTaskRequestKafkaMessage.builder()
                .requestId(taskRequestId)
                .partNumber(workersNumber)
                .partCount(wordsPerWorker + (i == workersNumber - 1 ? potentialWordsCount % workersNumber : 0))
                .hash(request.hash())
                .maxLength(request.maxLength())
                .build())
            .toList();

        crackHashTaskDistributed.distributedSendCrackHashTasks(taskRequests);

        return StartCrackingHashProcessResponse.builder()
            .requestId(taskRequestId)
            .build();
    }

    @Override
    public void handleCrackHashTaskResult(CrackHashTaskResultKafkaMessage result) {
        log.info("Received crack hash task result: {}", result);
    }
}

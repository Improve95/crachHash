package ru.nsu.crackhash.manager.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.config.alphabet.AlphabetConfig;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskRequestKafkaMessage;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashWorkerTask;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashWorkerTaskStatus;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;
import ru.nsu.crackhash.manager.core.service.CrackingHashTaskService;
import ru.nsu.crackhash.manager.core.service.HashWordService;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashWordServiceImpl implements HashWordService {

    private final CrackHashTaskDistributed crackHashTaskDistributed;

    private final CrackingHashTaskService crackingHashTaskServiceImpl;

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

        List<CrackHashTaskRequestKafkaMessage> taskRequestsKafkaMessages = new ArrayList<>();
        List<CrackingHashWorkerTask> crackingHashWorkerTask = new ArrayList<>();
        for (int i = 0; i < workersNumber; i++) {
            int partNumber = i;
            long partCount = wordsPerWorker +
                (i == workersNumber - 1 ? potentialWordsCount % workersNumber : 0);

            var workerTaskRequestId = UUID.randomUUID();
            taskRequestsKafkaMessages.add(
                CrackHashTaskRequestKafkaMessage.builder()
                    .requestId(workerTaskRequestId)
                    .partNumber(partNumber)
                    .partCount(partCount)
                    .hash(request.hash())
                    .maxLength(request.maxLength())
                    .build()
            );

            crackingHashWorkerTask.add(
                CrackingHashWorkerTask.builder()
                    .id(workerTaskRequestId)
                    .parentCrackingHashTaskId(taskRequestId)
                    .partNumber(partNumber)
                    .partCount(partCount)
                    .status(CrackingHashWorkerTaskStatus.IN_PROGRESS)
                    .build()
            );
        }

        crackingHashTaskServiceImpl.createCrackHash(
            CrackingHashTask.builder()
                .id(taskRequestId)
                .hash(request.hash())
                .maxLength(request.maxLength())
                .startedAt(Instant.now())
                .build()
        );

        crackingHashWorkerTask.forEach(crackingHashTaskServiceImpl::createCrackHashWorkerTask);

        crackHashTaskDistributed.distributedSendCrackHashTasks(taskRequestsKafkaMessages);

        return StartCrackingHashProcessResponse.builder()
            .requestId(taskRequestId)
            .build();
    }
}

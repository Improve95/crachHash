package ru.nsu.crackhash.manager.core.service.impl.hashword;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.config.alphabet.AlphabetConfig;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.service.CrackingTaskService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CrackingTaskServiceImpl implements CrackingTaskService {

    private final AlphabetConfig alphabetConfig;

    @Value("${worker.number}")
    private int workersNumber;

    @Override
    public List<CrackHashTaskWorkerRequest> createCrackRequest(UUID taskId, StartCrackingHashProcessRequest request) {
        BigInteger bigInteger = BigInteger.valueOf(alphabetConfig.getAlphabet().size());
        bigInteger = bigInteger.pow(request.maxLength());
        long potentialWordsCount = bigInteger.longValue();

        long wordsPerWorker = potentialWordsCount / workersNumber;

        List<CrackHashTaskWorkerRequest> crackHashTaskWorkerRequests = new ArrayList<>();
        for (int i = 0; i < workersNumber; i++) {
            int partNumber = i;
            long partCount = wordsPerWorker +
                (i == workersNumber - 1 ? potentialWordsCount % workersNumber : 0);

            crackHashTaskWorkerRequests.add(
                CrackHashTaskWorkerRequest.builder()
                    .requestId(taskId)
                    .partNumber(partNumber)
                    .partCount(partCount)
                    .hash(request.hash())
                    .maxLength(request.maxLength())
                    .alphabet(alphabetConfig.getAlphabet())
                    .build()
            );
        }

        return crackHashTaskWorkerRequests;
    }
}

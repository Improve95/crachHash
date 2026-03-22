package ru.nsu.crackhash.worker.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;
import ru.nsu.crackhash.worker.core.service.crypto.CryptoService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HashCrackingServiceImpl implements HashCrackingService {

    private final CryptoService cryptoService;

    private final ResultService resultService;

    private final ExecutorService executorService;

    private final Map<UUID, Boolean> cancellationTokenMap = new ConcurrentHashMap<>();

    @Value("${crack-hash.execution-timeout}")
    private int timeoutTaskExecution;

    @Value("${crack-hash.send-progress-every-percent}")
    private long sendProgressEveryPercent;

    public HashCrackingServiceImpl(
        CryptoService cryptoService,
        ResultService resultService,
        @Qualifier("crackHashThreadPoolExecutor") ExecutorService executorService
    ) {
        this.cryptoService = cryptoService;
        this.resultService = resultService;
        this.executorService = executorService;
    }

    @Override
    public CompletableFuture<Boolean> createCrackHashTask(CreateCrackHashTaskRequest request) {
        UUID distributedWorkerTaskId = UUID.randomUUID();
        cancellationTokenMap.put(distributedWorkerTaskId, false);

        return CompletableFuture
            .runAsync(
                () -> {
                    List<String> answers = crackHash(request, distributedWorkerTaskId);
                    cancellationTokenMap.remove(distributedWorkerTaskId);

                    resultService.sendTaskResultToManager(
                        SendCrackResultRequest.builder()
                            .taskId(request.requestId())
                            .answers(answers)
                            .build()
                    );
                },
                executorService
            )
            .orTimeout(timeoutTaskExecution, TimeUnit.MILLISECONDS)
            .handleAsync((result, ex) -> {
                if (ex == null) {
                    log.info(
                        "success cracking hash task, id: {}",
                        request.requestId()
                    );
                    return true;
                } else {
                    cancellationTokenMap.computeIfPresent(distributedWorkerTaskId, (k, v) -> true);
                    log.error(
                        "failed cracking hash task, id: {}, cause: {}",
                        request.requestId(),
                        ExceptionUtils.getRootCauseMessage(ex)
                    );
                    return false;
                }
            });
    }

    /*private UUID startCrackingHash(CreateCrackHashTaskRequest request) {
        long totalWordsForCheck = request.partCount();
        List<String> answers = new ArrayList<>();

        UUID distributedWorkerTaskId = UUID.randomUUID();

        long diffChangeSizeForRequiredPercent = totalWordsForCheck / 100L * sendProgressEveryPercent;

        crackHash(request, totalWordsForCheck, answers, diffChangeSizeForRequiredPercent);
        resultService.sendTaskResultToManager(
            SendCrackResultRequest.builder()
                .taskId(request.requestId())
                .answers(answers)
                .build()
        );

        return distributedWorkerTaskId;
    }*/

    private List<String> crackHash(
        CreateCrackHashTaskRequest request,
        UUID distributedWorkerTaskId
    ) {
        long totalWordsForCheck = request.partCount();
        long diffChangeSizeForRequiredPercent = totalWordsForCheck / 100L * sendProgressEveryPercent;

        Iterator<String> wordsIterator = Generator.permutation(request.alphabet())
            .withRepetitions(request.maxLength())
            .stream()
            .skip(request.partCount() * request.partNumber())
            .limit(totalWordsForCheck)
            .map(wl -> String.join("", wl))
            .iterator();

        int checkedWords = 0;
        ArrayList<String> answers = new ArrayList<>();
        while (wordsIterator.hasNext() && !cancellationTokenMap.get(distributedWorkerTaskId)) {
            String word = wordsIterator.next();
            String hash = cryptoService.hashingByMd5(word.getBytes(StandardCharsets.UTF_8));
            if (request.hash().equals(hash)) {
                answers.add(word);
            }

            checkedWords++;
            boolean isSent = resultService.updateTaskProgress(
                checkedWords,
                diffChangeSizeForRequiredPercent,
                request.requestId()
            );

            if (isSent) {
                checkedWords = 0;
            }
        }

        return answers;
    }
}

package ru.nsu.crackhash.worker.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.paukov.combinatorics3.Generator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;
import ru.nsu.crackhash.worker.core.service.ResultService;
import ru.nsu.crackhash.worker.core.service.crypto.CryptoService;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashCrackingServiceImpl implements HashCrackingService {

    private final CryptoService cryptoService;

    private final ResultService resultService;

    @Async("crackHashThreadPoolExecutor")
    @Override
    public CompletableFuture<Boolean> createCrackHashTask(CreateCrackHashTaskRequest createCrackHashTaskRequest) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    var answers = Generator.permutation(createCrackHashTaskRequest.alphabet())
                        .withRepetitions(createCrackHashTaskRequest.maxLength())
                        .stream()
                        .skip(createCrackHashTaskRequest.partCount() * createCrackHashTaskRequest.partNumber())
                        .map(wl -> String.join("", wl))
                        .filter(word -> {
                            String hash = cryptoService.hashingByMd5(word.getBytes(StandardCharsets.UTF_8));
                            return createCrackHashTaskRequest.hash().equals(hash);
                        })
                        .toList();

                    resultService.sendResultToManager(
                        SendCrackResultRequest.builder()
                            .taskId(createCrackHashTaskRequest.requestId())
                            .answers(answers)
                            .build()
                    );

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
        );
    }
}

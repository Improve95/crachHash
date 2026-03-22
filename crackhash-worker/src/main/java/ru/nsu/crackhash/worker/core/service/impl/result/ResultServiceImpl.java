package ru.nsu.crackhash.worker.core.service.impl.result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.core.feign.manager.ManagerFeignClient;
import ru.nsu.crackhash.worker.core.feign.manager.dto.AddWorkerTaskProgressRequest;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;
import ru.nsu.crackhash.worker.core.service.ResultSender;
import ru.nsu.crackhash.worker.core.service.impl.ResultService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResultServiceImpl implements ResultService {

    private final ResultSender resultSender;

    private final ManagerFeignClient managerFeignClient;

    private final ExecutorService progressSenderVirtualThreadPoolExecutor =
        Executors.newVirtualThreadPerTaskExecutor();

    @Value("${crack-hash.send-progress-every-percent}")
    private double sendProgressEveryPercent;

    @Override
    public void sendTaskResultToManager(SendCrackResultRequest sendCrackResultRequest) {
        resultSender.sendResultToManager(sendCrackResultRequest);
    }

    @Override
    public boolean updateTaskProgress(long checkedWords, long totalWordCheck, UUID taskId) {
        double diffChangeSizeForRequiredPercent = totalWordCheck / 100D * sendProgressEveryPercent;
        if (checkedWords >= diffChangeSizeForRequiredPercent) {
            CompletableFuture.runAsync(
                () -> managerFeignClient.increaseTaskProgress(
                    AddWorkerTaskProgressRequest.builder()
                        .taskId(taskId)
                        .increaseProgressPercent(sendProgressEveryPercent)
                        .build()
                ),
                progressSenderVirtualThreadPoolExecutor
            );
            return true;
        }
        return false;
    }
}

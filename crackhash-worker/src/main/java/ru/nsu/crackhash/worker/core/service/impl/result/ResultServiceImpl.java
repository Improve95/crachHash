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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResultServiceImpl implements ResultService {

    private final ResultSender resultSender;

    private final ManagerFeignClient managerFeignClient;

//    private final Map<UUID, Long> workerDistributeTaskProgressRemainMap = new ConcurrentHashMap<>();

    private final ExecutorService progressSenderVirtualThreadPoolExecutor =
        Executors.newVirtualThreadPerTaskExecutor();

    @Value("${crack-hash.send-progress-every-percent}")
    private int sendProgressEveryPercent;

    @Override
    public void sendTaskResultToManager(SendCrackResultRequest sendCrackResultRequest) {
        resultSender.sendResultToManager(sendCrackResultRequest);
    }

    public void createDistributeTaskProgressTracking(UUID distributeTaskId, long totalWordsForCheck) {
        workerDistributeTaskProgressRemainMap.put(distributeTaskId, totalWordsForCheck);
    }

    public void deleteDistributeTaskProgressTracking(UUID distributeTaskId) {
        workerDistributeTaskProgressRemainMap.remove(distributeTaskId);
    }

    public void updateTaskProgress(long checkedWords, UUID taskId) {
        long wordsRemain = workerDistributeTaskProgressRemainMap.get(taskId);
        if (wordsRemain - checkedWords) {

        }

        workerDistributeTaskProgressRemainMap.computeIfPresent(taskId, (k, v) -> v - checkedWords);
        CompletableFuture.runAsync(
            () -> managerFeignClient.increaseTaskProgress(
                AddWorkerTaskProgressRequest.builder()
                    .taskId(taskId)
                    .increaseProgressPercent(2)
                    .build()
            ),
            progressSenderVirtualThreadPoolExecutor
        );
    }

    private record ProgressStatus(int remain, int diffChangeForRequiredPercent) {}
}

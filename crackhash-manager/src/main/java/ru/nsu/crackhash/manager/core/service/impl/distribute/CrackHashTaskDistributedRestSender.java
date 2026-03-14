package ru.nsu.crackhash.manager.core.service.impl.distribute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nsu.crackhash.manager.config.kafka.worker.WorkerConfig;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "send-type", havingValue = "rest")
@Service
public class CrackHashTaskDistributedRestSender implements CrackHashTaskDistributed {

    private static final String CREATE_TASK_URL = "/internal/api/worker/hash/crack/task";

    private final RestTemplate restTemplate = new RestTemplate();

    private final AtomicInteger currentWorker = new AtomicInteger(0);

    private final WorkerConfig workerConfig;

    @Override
    public void distributedSendCrackHashTasks(CrackHashTaskWorkerRequest request) {
        List<String> urls = workerConfig.urls();
        int currentWorkerNumber = currentWorker.getAndAccumulate(
            workerConfig.number(), (cur, wn) -> (cur + 1) % wn
        );

        String createTaskUrl = urls.get(currentWorkerNumber % workerConfig.number()) + CREATE_TASK_URL;
        log.info(
            "start send task request by rest with url: {}, partNumber: {}",
            createTaskUrl,
            request.partNumber()
        );
        restTemplate.postForEntity(
            createTaskUrl,
            request,
            Void.class
        );
    }
}

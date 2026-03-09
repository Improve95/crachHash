package ru.nsu.crackhash.manager.core.service.impl.distribute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.core.feign.worker.WorkerFeignClient;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrackHashTaskDistributedRestSender implements CrackHashTaskDistributed {

    private final WorkerFeignClient workerFeignClient;

    @Override
    public void distributedSendCrackHashTasks(List<CrackHashTaskWorkerRequest> requests) {
        requests.forEach(workerFeignClient::createCrackHashTask);
    }
}

package ru.nsu.crackhash.manager.core.service.impl.hashword;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.api.dto.ReceiveCrackResultRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;
import ru.nsu.crackhash.manager.core.service.CrackingHashTaskService;
import ru.nsu.crackhash.manager.core.service.HashWordService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTaskStatus.IN_PROGRESS;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashWordServiceKafkaImpl implements HashWordService {

    private final CrackingHashTaskService crackingHashTaskService;

    private final CrackHashTaskDistributed crackHashTaskDistributed;

    private final TaskRepo taskRepo;

    @Override
    public StartCrackingHashProcessResponse startCrackHash(StartCrackingHashProcessRequest request) {
        UUID taskId = UUID.randomUUID();
        List<CrackHashTaskWorkerRequest> tasksList = crackingHashTaskService.createCrackRequest(taskId, request);

        int queueSize = taskRepo.putInQueue(
            CrackingHashTask.builder()
                .id(taskId)
                .hash(request.hash())
                .maxLength(request.maxLength())
                .status(IN_PROGRESS)
                .startedAt(Instant.now())
                .build()
        );

        if (queueSize == 1) {
            distributeSend(tasksList);
        }

        return StartCrackingHashProcessResponse.builder()
            .requestId(taskId)
            .build();
    }

    @Override
    public void receiveCrackHashResult(ReceiveCrackResultRequest request) {
        taskRepo.addAnswers(request.taskId(), request.answers());

        CrackingHashTask task = taskRepo.getTask(request.taskId());
        if (task.getAnswers().size() == task.getTaskPartCount()) {
            CrackingHashTask crackingHashTask = taskRepo.removeFromQueue();
            startCrackHash(
                StartCrackingHashProcessRequest.builder()
                    .hash(crackingHashTask.getHash())
                    .maxLength(crackingHashTask.getMaxLength())
                    .build()
            );
        }
    }

    @Override
    public void distributeSend(List<CrackHashTaskWorkerRequest> tasksList) {
        crackHashTaskDistributed.distributedSendCrackHashTasks(tasksList);
    }
}

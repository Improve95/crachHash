package ru.nsu.crackhash.manager.core.service.impl.hashword;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.api.dto.GetCrackHashProcessStatusResponse;
import ru.nsu.crackhash.manager.api.dto.ReceiveCrackResultRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;
import ru.nsu.crackhash.manager.core.service.CrackingTaskService;
import ru.nsu.crackhash.manager.core.service.HashWordService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTaskStatus.HALF_READY;
import static ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTaskStatus.IN_PROGRESS;
import static ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTaskStatus.READY;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashWordServiceImpl implements HashWordService {

    private final CrackingTaskService crackingTaskService;

    private final CrackHashTaskDistributed crackHashTaskDistributed;

    private final TaskRepo taskRepo;

    @Override
    public StartCrackingHashProcessResponse startCrackHash(StartCrackingHashProcessRequest request) {
        UUID taskId = UUID.randomUUID();
        List<CrackHashTaskWorkerRequest> tasksList = crackingTaskService.createCrackRequest(taskId, request);

        int queueSize = taskRepo.putInQueue(
            CrackingHashTask.builder()
                .id(taskId)
                .hash(request.hash())
                .maxLength(request.maxLength())
                .status(IN_PROGRESS)
                .currentCompletedTaskPartCount(0)
                .taskPartCount(tasksList.size())
                .startedAt(Instant.now())
                .answers(new ArrayList<>())
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

        if (task == null) return;

        if (task.getTaskPartCount() == task.getCurrentCompletedTaskPartCount()) {
            taskRepo.removeFromQueue();
            task.setStatus(READY);
            runTaskFromQueue();
        } else {
            task.setStatus(HALF_READY);
        }
    }

    @Override
    public void distributeSend(List<CrackHashTaskWorkerRequest> tasksList) {
        crackHashTaskDistributed.distributedSendCrackHashTasks(tasksList);
    }

    @Override
    public GetCrackHashProcessStatusResponse getCrackingHashStatus(UUID requestId) {
        CrackingHashTask crackingHashTask = taskRepo.getTask(requestId);

        if (crackingHashTask == null) {
            return null;
        }

        return GetCrackHashProcessStatusResponse.builder()
            .crackingHashTaskStatus(crackingHashTask.getStatus())
            .answers(crackingHashTask.getAnswers())
            .build();
    }

    private void runTaskFromQueue() {
        CrackingHashTask crackingHashTask = taskRepo.getFromQueue();
        if (crackingHashTask != null) {
            startCrackHash(
                StartCrackingHashProcessRequest.builder()
                    .hash(crackingHashTask.getHash())
                    .maxLength(crackingHashTask.getMaxLength())
                    .build()
            );
        }
    }
}

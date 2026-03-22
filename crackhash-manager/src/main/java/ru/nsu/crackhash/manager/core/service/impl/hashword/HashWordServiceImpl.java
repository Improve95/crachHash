package ru.nsu.crackhash.manager.core.service.impl.hashword;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.api.dto.AddWorkerProgressRequest;
import ru.nsu.crackhash.manager.api.dto.GetCrackHashProcessStatusResponse;
import ru.nsu.crackhash.manager.api.dto.ReceiveCrackResultRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;
import ru.nsu.crackhash.manager.core.service.CrackHashTaskDistributed;
import ru.nsu.crackhash.manager.core.service.CrackingTaskService;
import ru.nsu.crackhash.manager.core.service.HashWordService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.HALF_READY;
import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.IN_PROGRESS;
import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.READY;
import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.WAITING;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashWordServiceImpl implements HashWordService {

    private final CrackingTaskService crackingTaskService;

    private final CrackHashTaskDistributed crackHashTaskDistributed;

    private final TaskRepo taskRepo;

    @Value("${worker.number}")
    private int workerNumber;

    @Override
    public StartCrackingHashProcessResponse addCrackHashTaskInQueue(StartCrackingHashProcessRequest request) {
        UUID taskId = UUID.randomUUID();

        taskRepo.putInQueue(
            CrackingHashTask.builder()
                .id(taskId)
                .hash(request.hash())
                .maxLength(request.maxLength())
                .status(WAITING)
                .currentCompletedTaskPartCount(0)
                .taskPartCount(-1)
                .startedAt(null)
                .answers(new ArrayList<>())
                .build()
        );

        runTaskFromQueue();

        return StartCrackingHashProcessResponse.builder()
            .requestId(taskId)
            .build();
    }

    @Override
    public void receiveCrackHashResult(ReceiveCrackResultRequest request) {
        taskRepo.addAnswers(request.taskId(), request.answers());
        CrackingHashTask task = taskRepo.getTask(request.taskId());

        if (task == null) return;

        task.setUpdatedAt(Instant.now());
        if (task.getTaskPartCount() == task.getCurrentCompletedTaskPartCount()) {
            task.setStatus(READY);
            taskRepo.update(task.getId(), task);
        } else {
            task.setStatus(HALF_READY);
            taskRepo.update(task.getId(), task);
        }

        runTaskFromQueue();
    }

    @Override
    public GetCrackHashProcessStatusResponse getCrackingHashStatus(UUID requestId) {
        CrackingHashTask task = taskRepo.getTask(requestId);

        if (task == null) {
            return null;
        }

        runTaskFromQueue();

        return GetCrackHashProcessStatusResponse.builder()
            .crackingHashTaskStatus(task.getStatus())
            .answers(task.getAnswers())
            .progress(task.getStatus() == READY ? 100 : (int) Math.ceil(task.getProgress()))
            .build();
    }

    @Override
    public void increaseTaskProgress(AddWorkerProgressRequest request) {
        taskRepo.increaseProgress(request.taskId(), request.increaseProgressPercent() / (double) workerNumber);
    }

    private void runTaskFromQueue() {
        taskRepo.markHungTask();
        CrackingHashTask crackingHashTask = taskRepo.getFirstWaitingTask();
        if (crackingHashTask != null) {
            var request = StartCrackingHashProcessRequest.builder()
                .hash(crackingHashTask.getHash())
                .maxLength(crackingHashTask.getMaxLength())
                .build();

            List<CrackHashTaskWorkerRequest> workerRequests = crackingTaskService.createCrackRequest(
                crackingHashTask.getId(), request
            );

            crackingHashTask.setStatus(IN_PROGRESS);
            crackingHashTask.setTaskPartCount(workerRequests.size());
            crackingHashTask.setStartedAt(Instant.now());
            crackingHashTask.setUpdatedAt(Instant.now());
            taskRepo.update(crackingHashTask.getId(), crackingHashTask);

            int successSend = distributeSend(workerRequests);
            if (successSend == 0) {
                var task = taskRepo.getTask(workerRequests.getFirst().requestId());
                task.setStatus(WAITING);
                taskRepo.update(task.getId(), task);
            }
        }
    }

    private int distributeSend(List<CrackHashTaskWorkerRequest> workerRequests) {
        int successSend = 0;
        for (var workerRequest : workerRequests) {
            try {
                crackHashTaskDistributed.distributedSendCrackHashTasks(workerRequest);
                successSend++;
                log.info(
                    "success distribute send task request to worker with requestId: {}, partNumber: {}",
                    workerRequest.requestId(),
                    workerRequest.partNumber()
                );
            } catch (Exception ex) {
                log.error(
                    "failed distribute send task request to worker with requestId: {}, partNumber: {}, cause: {}",
                    workerRequest.requestId(),
                    workerRequest.partNumber(),
                    ExceptionUtils.getRootCauseMessage(ex)
                );
            }
        }
        return successSend;
    }
}

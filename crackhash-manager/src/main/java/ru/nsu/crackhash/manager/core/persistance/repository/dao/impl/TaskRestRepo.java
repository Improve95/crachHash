package ru.nsu.crackhash.manager.core.persistance.repository.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.config.kafka.task.TaskProperties;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.FAILED;
import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.HALF_READY;
import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.IN_PROGRESS;
import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.READY;
import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.WAITING;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "send-type", havingValue = "rest")
@Component
public class TaskRestRepo implements TaskRepo {

    private final Queue<UUID> crackingHashQueue = new ConcurrentLinkedDeque<>();

    private final Map<UUID, CrackingHashTask> crackingHashTaskMap = new ConcurrentHashMap<>();

    private final Map<UUID, ReentrantLock> mutexMap = new ConcurrentHashMap<>();

    private final TaskProperties taskProperties;

    @Override
    public long putInQueue(CrackingHashTask crackingHashTask) {
        crackingHashTaskMap.putIfAbsent(crackingHashTask.getId(), crackingHashTask);
        crackingHashQueue.add(crackingHashTask.getId());
        return crackingHashQueue.size();
    }

    @Override
    public void markHungTask() {
        for (UUID taskId : crackingHashQueue) {
            CrackingHashTask task = crackingHashTaskMap.get(taskId);
            CrackingHashTaskStatus status = task.getStatus();
            Instant startedAt = task.getStartedAt();
            if (status == IN_PROGRESS &&
                    startedAt.isBefore(Instant.now().minus(taskProperties.inProcessLifetimeDurationThreshold())) ||
                status == HALF_READY &&
                    startedAt.isBefore(Instant.now().minus(taskProperties.halfReadyLifetimeDurationThreshold()))) {
                task.setStatus(FAILED);
            }
        }
    }

    @Override
    public CrackingHashTask getFirstWaitingTask() {
        for (UUID taskId : crackingHashQueue) {
            CrackingHashTask crackingHashTask = crackingHashTaskMap.get(taskId);
            if (crackingHashTask.getStatus() == WAITING) {
                return crackingHashTask;
            } else if (crackingHashTask.getStatus() == READY) {
                continue;
            } else if (crackingHashTask.getStatus() == IN_PROGRESS
                || crackingHashTask.getStatus() == HALF_READY) {
                return null;
            }
        }
        return null;
    }

    @Override
    public CrackingHashTask getTask(UUID taskId) {
        return crackingHashTaskMap.get(taskId);
    }

    @Override
    public void addAnswers(UUID taskId, List<String> newAnswers) {
        var mutex = mutexMap.computeIfAbsent(taskId, k -> new ReentrantLock());
        try {
            mutex.lock();
            var task = crackingHashTaskMap.get(taskId);
            if (task != null) {
                task.getAnswers().addAll(newAnswers);
                task.setCurrentCompletedTaskPartCount(task.getCurrentCompletedTaskPartCount() + 1);
            }
            mutex.unlock();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void increaseProgress(UUID taskId, double addProgress) {
        var mutex = mutexMap.computeIfAbsent(taskId, k -> new ReentrantLock());
        try {
            mutex.lock();
            var task = crackingHashTaskMap.get(taskId);
            if (task != null) {
                task.setProgress(task.getProgress() + addProgress);
            }
            mutex.unlock();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(UUID taskId, CrackingHashTask task) {
        crackingHashTaskMap.computeIfPresent(taskId, (k, v) -> v);
    }
}

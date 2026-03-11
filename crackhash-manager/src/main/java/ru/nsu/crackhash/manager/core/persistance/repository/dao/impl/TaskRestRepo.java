package ru.nsu.crackhash.manager.core.persistance.repository.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

import static ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus.WAITING;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "send-type", havingValue = "rest")
@Component
public class TaskRestRepo implements TaskRepo {

    private final Queue<UUID> crackingHashQueue = new ConcurrentLinkedDeque<>();

    private final Map<UUID, CrackingHashTask> crackingHashTaskMap = new ConcurrentHashMap<>();

    private final Map<UUID, Semaphore> taskSemaphoreMap = new ConcurrentHashMap<>();

    @Override
    public long putInQueue(CrackingHashTask crackingHashTask) {
        crackingHashTaskMap.putIfAbsent(crackingHashTask.getId(), crackingHashTask);
        crackingHashQueue.add(crackingHashTask.getId());
        return crackingHashQueue.size();
    }

    @Override
    public CrackingHashTask getFromQueue() {
        UUID taskId = crackingHashQueue.peek();
        if (taskId != null) {
            return crackingHashTaskMap.get(taskId);
        }
        return null;
    }

    @Override
    public CrackingHashTask removeFromQueue() {
        UUID taskId = crackingHashQueue.poll();
        if (taskId != null) {
            return crackingHashTaskMap.get(taskId);
        }
        return null;
    }

    @Override
    public CrackingHashTask getFirstWaitingTask() {
        return crackingHashQueue.stream()
            .filter(taskId -> crackingHashTaskMap.get(taskId).getStatus().equals(WAITING))
            .findFirst()
            .map(crackingHashTaskMap::get)
            .orElse(null);
    }

    @Override
    public CrackingHashTask getTask(UUID taskId) {
        return crackingHashTaskMap.get(taskId);
    }

    @Override
    public void addAnswers(UUID taskId, List<String> newAnswers) {
        var taskSemaphore = taskSemaphoreMap.computeIfAbsent(taskId, k -> new Semaphore(1));
        try {
            taskSemaphore.acquire();
            var task = crackingHashTaskMap.get(taskId);
            if (task != null) {
                task.getAnswers().addAll(newAnswers);
                task.setCurrentCompletedTaskPartCount(task.getCurrentCompletedTaskPartCount() + 1);
            }
            taskSemaphore.release();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void updateTaskRequest(UUID taskId, CrackingHashTask task) {
        crackingHashTaskMap.computeIfPresent(taskId, (k, v) -> v);
    }
}

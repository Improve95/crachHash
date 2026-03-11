package ru.nsu.crackhash.manager.core.persistance.repository.dao;

import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;

import java.util.List;
import java.util.UUID;

public interface TaskRepo {

    long putInQueue(CrackingHashTask crackingHashTask);

    CrackingHashTask getFromQueue();

    CrackingHashTask removeFromQueue();

    CrackingHashTask getTask(UUID taskId);

    void addAnswers(UUID taskId, List<String> answers);

    void updateTaskRequest(UUID taskId, CrackingHashTask task);
}

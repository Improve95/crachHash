package ru.nsu.crackhash.manager.core.persistance.repository.dao;

import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;

import java.util.List;
import java.util.UUID;

public interface TaskRepo {

    long putInQueue(CrackingHashTask crackingHashTask);

    void markHungTask();

    CrackingHashTask getFirstWaitingTask();

    CrackingHashTask getTask(UUID taskId);

    void addAnswers(UUID taskId, List<String> answers);

    void increaseProgress(UUID taskId, int addProgress);

    void update(UUID taskId, CrackingHashTask task);
}

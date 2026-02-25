package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashWorkerTask;

public interface CrackingHashTaskService {

    void createCrackHash(CrackingHashTask crackingHashTask);

    void createCrackHashWorkerTask(CrackingHashWorkerTask crackingHashWorkerTask);
}

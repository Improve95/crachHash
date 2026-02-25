package ru.nsu.crackhash.manager.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashWorkerTask;
import ru.nsu.crackhash.manager.core.persistance.repository.CrackHashTaskRepository;
import ru.nsu.crackhash.manager.core.persistance.repository.CrackHashWorkerTaskRepository;
import ru.nsu.crackhash.manager.core.service.CrackingHashTaskService;

@RequiredArgsConstructor
@Service
public class CrackingHashTaskServiceImpl implements CrackingHashTaskService {

    private final CrackHashTaskRepository crackHashTaskRepository;

    private final CrackHashWorkerTaskRepository crackHashWorkerTaskRepository;

    @Override
    public void createCrackHash(CrackingHashTask crackingHashTask) {
        crackHashTaskRepository.save(crackingHashTask);
    }

    @Override
    public void createCrackHashWorkerTask(CrackingHashWorkerTask crackingHashWorkerTask) {
        crackHashWorkerTaskRepository.save(crackingHashWorkerTask);
    }
}

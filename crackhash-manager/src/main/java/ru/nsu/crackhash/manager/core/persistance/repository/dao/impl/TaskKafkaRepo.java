package ru.nsu.crackhash.manager.core.persistance.repository.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.config.kafka.KafkaConfig;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.repository.CrackHashTaskRepository;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class TaskKafkaRepo implements TaskRepo {

    private final CrackHashTaskRepository crackHashTaskRepository;

    @Override
    public int putInQueue(CrackingHashTask crackingHashTask) {
        return 1;
    }

    @Override
    public CrackingHashTask getFromQueue() {
        return null;
    }

    @Override
    public CrackingHashTask removeFromQueue() {
        return null;
    }

    @Override
    public CrackingHashTask getTask(UUID taskId) {
        return null;
    }

    @Override
    public void addAnswers(UUID taskId, List<String> answers) {

    }
}

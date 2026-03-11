package ru.nsu.crackhash.manager.core.persistance.repository.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.config.kafka.KafkaConfig;
import ru.nsu.crackhash.manager.core.persistance.model.queue.CrackHashTaskQueue;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class TaskKafkaRepo implements TaskRepo {

    private final MongoTemplate mongoTemplate;

    @Override
    public long putInQueue(CrackingHashTask crackingHashTask) {
        CrackHashTaskQueue crackHashTaskQueue = mongoTemplate.findOne(
            lastTaskInQueueQuery(),
            CrackHashTaskQueue.class
        );

        int prevPos = 0;
        if (crackHashTaskQueue != null) {
            prevPos = crackHashTaskQueue.getPosition();
        }

        mongoTemplate.insert(
            CrackHashTaskQueue.builder()
                .position(prevPos + 1)
                .taskId(crackingHashTask.getId())
                .build()
        );
        mongoTemplate.insert(crackingHashTask);

        return mongoTemplate.count(new Query(), CrackHashTaskQueue.class);
    }

    @Override
    public CrackingHashTask getFromQueue() {
        CrackHashTaskQueue crackHashTaskQueue = mongoTemplate.findOne(
            firstTaskInQueueQuery(),
            CrackHashTaskQueue.class
        );

        if (crackHashTaskQueue != null) {
            return getTask(crackHashTaskQueue.getTaskId());
        }

        return null;
    }

    @Override
    public CrackingHashTask getFirstWaitingTask() {
        return null;
    }

    @Override
    public CrackingHashTask removeFromQueue() {
        CrackHashTaskQueue crackHashTaskQueue = mongoTemplate.findAndRemove(
            firstTaskInQueueQuery(),
            CrackHashTaskQueue.class
        );

        if (crackHashTaskQueue != null) {
            return getTask(crackHashTaskQueue.getTaskId());
        }

        return null;
    }

    @Override
    public CrackingHashTask getTask(UUID taskId) {
        return mongoTemplate.findOne(
            taskByUuidQuery(taskId),
            CrackingHashTask.class
        );
    }

    @Override
    public void addAnswers(UUID taskId, List<String> answers) {
        Update update = new Update()
            .push("answers").each(answers)
            .inc("currentCompletedTaskPartCount", 1);

        FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        mongoTemplate.findAndModify(taskByUuidQuery(taskId), update, options, CrackingHashTask.class);
    }

    @Override
    public void updateTaskRequest(UUID taskId, CrackingHashTask task) {
        Update update = new Update()
            .set("status", task.getStatus());

        FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        mongoTemplate.findAndModify(taskByUuidQuery(taskId), update, options, CrackingHashTask.class);
    }

    private Query firstTaskInQueueQuery() {
        return new Query()
            .with(Sort.by(Sort.Direction.DESC, "position"))
            .limit(1);
    }

    private Query lastTaskInQueueQuery() {
        return new Query()
            .with(Sort.by(Sort.Direction.ASC, "position"))
            .limit(1);
    }

    private Query taskByUuidQuery(UUID id) {
        return new Query()
            .addCriteria(Criteria.where("id").is(id))
            .limit(1);
    }
}

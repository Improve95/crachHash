package ru.nsu.crackhash.manager.core.persistance.repository.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import ru.nsu.crackhash.manager.config.kafka.KafkaConfig;
import ru.nsu.crackhash.manager.config.kafka.task.TaskProperties;
import ru.nsu.crackhash.manager.core.persistance.model.queue.CrackHashTaskQueue;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTask;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus;
import ru.nsu.crackhash.manager.core.persistance.repository.dao.TaskRepo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@ConditionalOnBean(KafkaConfig.class)
@Component
public class TaskKafkaRepo implements TaskRepo {

    private final MongoTemplate mongoTemplate;

    private final TaskProperties taskProperties;

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
    public void markHungTask() {
        Instant now = Instant.now();

        Criteria inProgressStale = Criteria.where("status").is(CrackingHashTaskStatus.IN_PROGRESS)
            .and("updatedAt").lt(now.minus(taskProperties.inProcessLifetimeDurationThreshold()));

        Criteria halfReadyStale = Criteria.where("status").is(CrackingHashTaskStatus.HALF_READY)
            .and("updatedAt").lt(now.minus(taskProperties.halfReadyLifetimeDurationThreshold()));

        Query query = new Query(
            new Criteria().orOperator(
                inProgressStale,
                halfReadyStale
            )
        );

        Update update = new Update()
            .set("status", CrackingHashTaskStatus.FAILED);

        mongoTemplate.updateMulti(query, update, CrackingHashTask.class)
            .getModifiedCount();
    }

    @Override
    public CrackingHashTask getFirstWaitingTask() {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.sort(Sort.by("position")),
            Aggregation.lookup(
                "cracking_hash_tasks",
                "taskId",
                "_id",
                "task"
            ),
            Aggregation.unwind("task"),
            Aggregation.match(
                Criteria.where("task.status").is(CrackingHashTaskStatus.WAITING)
            ),
            Aggregation.replaceRoot("task"),
            Aggregation.limit(1)
        );

        AggregationResults<CrackingHashTask> results =
            mongoTemplate.aggregate(
                aggregation,
                "cracking_hash_tasks_queue",
                CrackingHashTask.class
            );

        return results.getUniqueMappedResult();
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
    public void update(UUID taskId, CrackingHashTask task) {
        Update update = new Update()
            .set("status", task.getStatus())
            .set("taskPartCount", task.getTaskPartCount())
            .set("startedAt", task.getStartedAt())
            .set("updatedAt", task.getUpdatedAt());

        FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        mongoTemplate.findAndModify(taskByUuidQuery(taskId), update, options, CrackingHashTask.class);
    }

    private Query firstTaskInQueueQuery() {
        return new Query()
            .with(Sort.by(Sort.Direction.ASC, "position"))
            .limit(1);
    }

    private Query lastTaskInQueueQuery() {
        return new Query()
            .with(Sort.by(Sort.Direction.DESC, "position"))
            .limit(1);
    }

    private Query taskByUuidQuery(UUID id) {
        return new Query()
            .addCriteria(Criteria.where("id").is(id))
            .limit(1);
    }
}

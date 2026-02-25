package ru.nsu.crackhash.manager.core.persistance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("cracking_hash_workers_tasks")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrackingHashWorkerTask {

    @Id
    private UUID id;

    private UUID parentCrackingHashTaskId;

    private int partNumber;

    private long partCount;

    private CrackingHashWorkerTaskStatus status;
}

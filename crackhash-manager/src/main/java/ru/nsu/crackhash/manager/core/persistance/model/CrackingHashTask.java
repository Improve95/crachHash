package ru.nsu.crackhash.manager.core.persistance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document("cracking_hash_tasks")
public class CrackingHashTask {

    @Id
    private UUID id;

    private String hash;

    private int maxLength;

    private Map<Integer, List<String>> workerResultMap;

    private CrackingHashTaskStatus status;

    private Instant startedAt;
}

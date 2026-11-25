package ru.nsu.crackhash.manager.config.cluster;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ClusterConfig(Map<String, WorkerConfig> workers) {

    public ClusterConfig {
        if (workers == null || workers.isEmpty()) {
            throw new IllegalArgumentException("At least one worker is required");
        }
        workers.forEach((id, worker) -> {
            if (id == null || id.isBlank() || worker == null || worker.getUrl() == null || worker.getUrl().isBlank()) {
                throw new IllegalArgumentException("Each worker must have a non-blank id and url");
            }
        });
        workers = Collections.unmodifiableMap(new LinkedHashMap<>(workers));
    }
}

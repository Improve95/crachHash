package ru.nsu.crackhash.manager.config.cluster;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.List;

@ConfigurationProperties(prefix = "cluster")
public record ClusterProperties(
    ClusterConfigType configType,
    Path infoPath,
    int workerNumber,
    List<String> workerUris
) {}

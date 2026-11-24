package ru.nsu.crackhash.manager.config.cluster;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Valid
@ConfigurationProperties(prefix = "cluster")
public record ClusterConfig(Map<String, WorkerConfig> workers) {}

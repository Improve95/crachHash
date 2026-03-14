package ru.nsu.crackhash.manager.config.kafka.task;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "crack-hash.task")
public record TaskProperties(
    Duration inProcessLifetimeDurationThreshold,
    Duration halfReadyLifetimeDurationThreshold
) {}

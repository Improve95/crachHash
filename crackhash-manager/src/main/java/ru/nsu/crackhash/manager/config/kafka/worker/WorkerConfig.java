package ru.nsu.crackhash.manager.config.kafka.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "worker")
public record WorkerConfig(int number, List<String> urls) {}

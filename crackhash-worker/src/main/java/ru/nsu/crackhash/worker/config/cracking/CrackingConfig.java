package ru.nsu.crackhash.worker.config.cracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class CrackingConfig {

    @Value("${crack-hash.cracking-hash-thread-pool-size}")
    private int crackingHashThreadPollSize;

    @Bean
    public ExecutorService crackHashThreadPoolExecutor() {
        return Executors.newFixedThreadPool(crackingHashThreadPollSize);
    }
}

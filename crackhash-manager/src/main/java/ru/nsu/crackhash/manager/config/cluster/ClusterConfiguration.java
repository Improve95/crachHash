package ru.nsu.crackhash.manager.config.cluster;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ClusterProperties.class)
public class ClusterConfiguration {

    @Bean
    public ClusterConfig clusterConfig(ClusterProperties properties, Environment environment) throws IOException {
        return switch (properties.configType()) {
            case ENV -> environmentClusterConfig(properties);
            case FILE -> fileClusterConfig(properties, environment);
        };
    }

    private ClusterConfig environmentClusterConfig(ClusterProperties properties) {
        int workerNumber = properties.workerNumber();
        var workerUris = properties.workerUris();
        if (workerNumber < 1 || workerUris == null || workerNumber != workerUris.size()) {
            throw new IllegalArgumentException("WORKER_NUMBER must be positive and equal the number of WORKER_URIS");
        }
        var workers = new LinkedHashMap<String, WorkerConfig>();
        for (int i = 0; i < workerNumber; i++) {
            var worker = new WorkerConfig();
            worker.setUrl(workerUrl(workerUris.get(i)));
            workers.put("worker-" + (i + 1), worker);
        }
        return new ClusterConfig(workers);
    }

    private String workerUrl(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("WORKER_URIS must not contain blank addresses");
        }
        String url = address.trim();
        if (!url.contains("://")) {
            url = "http://" + url;
        }
        URI uri = URI.create(url);
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
            || uri.getHost() == null || uri.getPort() == 0 || uri.getPort() > 65535) {
            throw new IllegalArgumentException("Invalid worker HTTP URL in WORKER_URIS: " + address);
        }
        return url;
    }

    private ClusterConfig fileClusterConfig(ClusterProperties properties, Environment environment) throws IOException {
        var resource = new FileSystemResource(properties.infoPath());
        var sources = new YamlPropertySourceLoader().load("clusterInfo", resource);
        var binder = new Binder(ConfigurationPropertySources.from(sources),
            new PropertySourcesPlaceholdersResolver(environment));
        var workers = binder
            .bind("cluster.workers", Bindable.mapOf(String.class, WorkerConfig.class))
            .orElseThrow(() -> new IllegalArgumentException("cluster.workers is missing from the cluster configuration"));
        return new ClusterConfig(workers);
    }
}

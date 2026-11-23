package ru.nsu.crackhash.manager.config.cluster;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Valid
@Data
public class WorkerConfig {

    @NotBlank
    private String url;
}

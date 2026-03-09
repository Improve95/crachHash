package ru.nsu.crackhash.manager.api.dto;

import java.util.List;
import java.util.UUID;

public record ReceiveCrackResultRequest(UUID taskId, List<String> answers) {}

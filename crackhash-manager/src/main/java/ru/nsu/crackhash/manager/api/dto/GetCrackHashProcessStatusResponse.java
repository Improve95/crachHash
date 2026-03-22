package ru.nsu.crackhash.manager.api.dto;

import lombok.Builder;
import ru.nsu.crackhash.manager.core.persistance.model.task.CrackingHashTaskStatus;

import java.util.List;

@Builder
public record GetCrackHashProcessStatusResponse(
    int progress,
    CrackingHashTaskStatus crackingHashTaskStatus,
    List<String> answers
) {}

package ru.nsu.crackhash.manager.api.dto;

import lombok.Builder;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTaskStatus;

import java.util.List;

@Builder
public record GetCrackHashProcessStatusResponse(CrackingHashTaskStatus crackingHashTaskStatus, List<String> data) {}

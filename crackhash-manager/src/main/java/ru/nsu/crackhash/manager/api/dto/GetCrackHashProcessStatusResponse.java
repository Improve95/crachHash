package ru.nsu.crackhash.manager.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GetCrackHashProcessStatusResponse(CrackingHashTaskStatus crackingHashTaskStatus, List<String> data) {}

package ru.nsu.crackhash.manager.api.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StartCrackingHashProcessResponse(UUID requestId) {}

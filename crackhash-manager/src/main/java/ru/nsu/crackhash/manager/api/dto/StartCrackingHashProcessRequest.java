package ru.nsu.crackhash.manager.api.dto;

import lombok.Builder;

@Builder
public record StartCrackingHashProcessRequest(String hash, int maxLength) {}

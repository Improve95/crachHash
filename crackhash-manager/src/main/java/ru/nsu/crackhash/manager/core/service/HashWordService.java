package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;

public interface HashWordService {

    StartCrackingHashProcessResponse startCrackHash(StartCrackingHashProcessRequest startFindWordProcessRequest);
}

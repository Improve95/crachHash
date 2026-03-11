package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.api.dto.GetCrackHashProcessStatusResponse;
import ru.nsu.crackhash.manager.api.dto.ReceiveCrackResultRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;

import java.util.UUID;

public interface HashWordService {

    StartCrackingHashProcessResponse addCrackHashTaskInQueue(StartCrackingHashProcessRequest startFindWordProcessRequest);

    void receiveCrackHashResult(ReceiveCrackResultRequest request);

    GetCrackHashProcessStatusResponse getCrackingHashStatus(UUID requestId);
}

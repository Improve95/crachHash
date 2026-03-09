package ru.nsu.crackhash.manager.core.service;

import ru.nsu.crackhash.manager.api.dto.ReceiveCrackResultRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;

import java.util.List;

public interface HashWordService {

    StartCrackingHashProcessResponse startCrackHash(StartCrackingHashProcessRequest startFindWordProcessRequest);

    void receiveCrackHashResult(ReceiveCrackResultRequest request);

    void distributeSend(List<CrackHashTaskWorkerRequest> tasksList);
}

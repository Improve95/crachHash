package ru.nsu.crackhash.worker.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;
import ru.nsu.crackhash.worker.core.service.HashCrackingService;

@RequiredArgsConstructor
@Service
public class HashCrackingServiceImpl implements HashCrackingService {

    @Override
    public void createCrackHashTask(CreateCrackHashTaskRequest createCrackHashTaskRequest) {

    }
}

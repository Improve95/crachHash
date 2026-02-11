package ru.nsu.crackhash.manager.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessRequest;
import ru.nsu.crackhash.manager.api.dto.StartCrackingHashProcessResponse;
import ru.nsu.crackhash.manager.configuration.AlphabetConfig;
import ru.nsu.crackhash.manager.core.service.HashWordProcessor;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashWordProcessorImpl implements HashWordProcessor {

    private final AlphabetConfig alphabetConfig;

    @Override
    public StartCrackingHashProcessResponse findWordFromHash(StartCrackingHashProcessRequest request) {
        return null;
    }
}

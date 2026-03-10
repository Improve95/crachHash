package ru.nsu.crackhash.manager.config.alphabet;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlphabetConfig {

    @Getter
    private final List<String> alphabet = List.of("r", "i", "s", "e", "m");
}

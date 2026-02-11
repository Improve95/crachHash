package ru.nsu.crackhash.manager.config.alphabet;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class AlphabetConfig {

    @Getter
    private final char[] alphabet;

    public AlphabetConfig() {
        alphabet = new char[26];
        for (int c = 'a'; c <= 'z'; c++) {
            alphabet[c - 'a'] = (char) c;
        }
    }
}

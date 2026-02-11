package ru.nsu.crackhash.manager.configuration;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AlphabetConfig {

    public final Character[] ALPHABET;

    public AlphabetConfig() {
        ALPHABET = new Character[26];
        for (int c = 'a'; c <= 'z'; c++) {
            ALPHABET[c - 'a'] = (char) c;
        }
    }
}

package ru.nsu.crackhash.worker.core.service.crypto;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class CryptoService {

    private final MessageDigest md;

    public CryptoService() throws Exception {
        this.md = MessageDigest.getInstance("MD5");
    }

    public String hashingByMd5(String plainText) {
        var hashingBytes = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
        return new String(hashingBytes, StandardCharsets.UTF_8);
    }
}

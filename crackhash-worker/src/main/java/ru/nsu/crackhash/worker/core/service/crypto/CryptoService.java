package ru.nsu.crackhash.worker.core.service.crypto;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;

@Service
public class CryptoService {

    private final MessageDigest md;

    public CryptoService() throws Exception {
        this.md = MessageDigest.getInstance("MD5");
    }

    public String hashingByMd5(byte[] plainTextBytes) {
        byte[] messageDigest = md.digest(plainTextBytes);

        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}

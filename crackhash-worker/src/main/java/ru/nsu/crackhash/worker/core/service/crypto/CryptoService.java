package ru.nsu.crackhash.worker.core.service.crypto;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;

@Service
public class CryptoService {

    public String hashingByMd5(byte[] plainTextBytes) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md5.digest(plainTextBytes);
            BigInteger bigInt = new BigInteger(1, messageDigest);
            return String.format("%032x", bigInt);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }
}

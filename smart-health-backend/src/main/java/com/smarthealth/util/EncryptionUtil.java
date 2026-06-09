package com.smarthealth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    private final TextEncryptor encryptor;

    public EncryptionUtil(@Value("${jwt.secret}") String secretKey) {
        String salt = "5c0744940b6c4f8a";
        this.encryptor = Encryptors.text(secretKey, salt);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        return encryptor.encrypt(plaintext);
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        return encryptor.decrypt(ciphertext);
    }
}

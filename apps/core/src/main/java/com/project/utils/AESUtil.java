package com.project.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AESUtil {
    private final SecretKey secretKey;
    private final byte[] iv;

    @Autowired
    public AESUtil(@Value("${request-auth.key}") String key, @Value("${request-auth.iv}") String iv) {
        secretKey = new SecretKeySpec(key.getBytes(), "AES");
        this.iv = iv.getBytes();
    }

    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.trim().isEmpty()) {
            throw new IllegalArgumentException("Encrypted text cannot be null or empty");
        }

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            // Base64 디코딩 전에 유효성 검사
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText.trim());

            // 암호화된 데이터가 16바이트의 배수인지 확인
            if (encryptedBytes.length % 16 != 0) {
                throw new IllegalArgumentException("Encrypted data length must be multiple of 16, got: " + encryptedBytes.length);
            }

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 input: " + e.getMessage(), e);
        }
    }}

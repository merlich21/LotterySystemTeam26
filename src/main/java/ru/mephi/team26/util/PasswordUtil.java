package ru.mephi.team26.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    private static final MessageDigest DIGEST;

    static {
        try {
            DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static String hashPassword(String password) {
        byte[] hashBytes = DIGEST.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : hashBytes) {
            sb.append(String.format("%02x", hashByte));
        }
        return sb.toString();
    }
}

package ru.mephi.team26.util;

public class EnvUtil {

    public static String getOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}

package ru.mephi.team26.config;

import lombok.Getter;

@Getter
public class AppConfig {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final int port;

    public AppConfig(String jdbcUrl, String dbUser, String dbPassword, int port) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.port = port;
    }

    public static AppConfig fromEnv() {
        String jdbcUrl = getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/exam");
        String dbUser = getOrDefault("DB_USER", "postgres");
        String dbPassword = getOrDefault("DB_PASSWORD", "12345678");
        int port = Integer.parseInt(getOrDefault("APP_PORT", "8080"));
        return new AppConfig(jdbcUrl, dbUser, dbPassword, port);
    }

    private static String getOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
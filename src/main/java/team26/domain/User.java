package team26.domain;

import java.time.OffsetDateTime;

public record User(
        int id,
        String username,
        String hashedPassword,
        UserRole role,
        OffsetDateTime createdAt
) {}

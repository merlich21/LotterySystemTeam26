package team26.domain.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRecord(
        UUID id,
        String login,
        String hashedPassword,
        UserRole role,
        OffsetDateTime createdAt
) {}

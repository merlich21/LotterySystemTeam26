package team26.domain.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRecord(
        UUID id,
        String name,
        String surname,
        String login,
        String email,
        String phone,
        String hashedPassword,
        UserRole role,
        OffsetDateTime createdAt
) {}

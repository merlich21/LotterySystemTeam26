package team26.domain.draw;

import java.time.OffsetDateTime;

public record Draw(
        int id,
        String title,
        DrawStatus status,
        int numbersCount,
        int maxNumber,
        int createdBy,
        OffsetDateTime createdAt
) {}

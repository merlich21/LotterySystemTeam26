package team26.domain.draw;

import java.time.OffsetDateTime;

public record DrawRecord(
        Integer id,
        Integer drawNumber,
        String drawName,
        DrawStatus status,
        Integer totalTickets,
        OffsetDateTime createdAt
) {}

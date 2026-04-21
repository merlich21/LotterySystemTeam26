package team26.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record Ticket (
        int id,
        List<Integer> numbers,
        TicketStatus status,
        int userId,
        int drawId,
        OffsetDateTime createdAt,
        OffsetDateTime checkedAt
) {}

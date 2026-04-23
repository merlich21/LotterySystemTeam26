package team26.domain.lotteryDraw;

import java.time.OffsetDateTime;

public record LotteryDrawRecord(
        Integer id,
        Integer drawNumber,
        String drawName,
        LotteryDrawStatus status,
        Integer totalTickets,
        OffsetDateTime createdAt
) {}

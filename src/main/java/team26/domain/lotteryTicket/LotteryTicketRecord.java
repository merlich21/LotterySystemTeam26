package team26.domain.lotteryTicket;

import java.time.OffsetDateTime;
import java.util.List;

public record LotteryTicketRecord(
        Integer id,
        Integer userId,
        List<Integer> ticketNumbers,
        LotteryTicketStatus status,
        Integer lotteryDrawId,
        OffsetDateTime createdAt
) {}

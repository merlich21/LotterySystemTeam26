package team26.domain.lotteryDrawResult;

import java.time.OffsetDateTime;
import java.util.List;

public record LotteryDrawResultRecord(
        int drawId,
        List<Integer> winningNumbers,
        OffsetDateTime declaredAt
) {}

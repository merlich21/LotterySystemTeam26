package team26.domain.drawResult;

import java.time.OffsetDateTime;
import java.util.List;

public record DrawResult(
        int drawId,
        List<Integer> winningNumbers,
        OffsetDateTime declaredAt
) {}

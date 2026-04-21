package team26.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record DrawResults(
        int drawId,
        List<Integer> winningNumbers,
        OffsetDateTime declaredAt
) {}

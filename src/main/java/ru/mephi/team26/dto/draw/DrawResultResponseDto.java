package ru.mephi.team26.dto.draw;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class DrawResultResponseDto {
    private long drawId;
    private List<Integer> winningNumbers;
    private OffsetDateTime generatedAt;
}

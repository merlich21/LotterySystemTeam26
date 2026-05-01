package ru.mephi.team26.dto.draw;

import lombok.Getter;
import lombok.Setter;
import ru.mephi.team26.entity.DrawStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
public class DrawResponseDto {
    private long id;
    private String title;
    private DrawStatus status;
    private int numbersCount;
    private int maxNumber;
    private OffsetDateTime createdAt;
}

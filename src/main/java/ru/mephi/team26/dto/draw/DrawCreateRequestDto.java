package ru.mephi.team26.dto.draw;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawCreateRequestDto {
    private String title;
    private int numbersCount;
    private int maxNumber;
}

package ru.mephi.team26.mapper;

import ru.mephi.team26.dto.draw.DrawResultResponseDto;
import ru.mephi.team26.entity.DrawResult;

public class DrawResultMapper {

    public DrawResultResponseDto entityToResponseDto(DrawResult entity) {
        DrawResultResponseDto responseDto = new DrawResultResponseDto();
        responseDto.setDrawId(entity.getId());
        responseDto.setWinningNumbers(entity.getWinningNumbers());
        responseDto.setGeneratedAt(entity.getGeneratedAt());
        return responseDto;
    }
}

package ru.mephi.team26.mapper.impl;

import ru.mephi.team26.dto.draw.DrawResultResponseDto;
import ru.mephi.team26.entity.DrawResult;
import ru.mephi.team26.mapper.ResponseMapper;

public class DrawResultMapper implements ResponseMapper<DrawResult, DrawResultResponseDto> {

    @Override
    public DrawResultResponseDto entityToResponseDto(DrawResult entity) {
        DrawResultResponseDto responseDto = new DrawResultResponseDto();
        responseDto.setDrawId(entity.getId());
        responseDto.setWinningNumbers(entity.getWinningNumbers());
        responseDto.setGeneratedAt(entity.getGeneratedAt());
        return responseDto;
    }
}

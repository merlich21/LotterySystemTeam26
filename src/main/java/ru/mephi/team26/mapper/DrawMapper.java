package ru.mephi.team26.mapper;

import ru.mephi.team26.dto.draw.DrawCreateRequestDto;
import ru.mephi.team26.dto.draw.DrawResponseDto;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.entity.DrawStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DrawMapper {

    public Draw requestDtoToEntity(DrawCreateRequestDto requestDto) {
        Draw entity = new Draw();
        entity.setTitle(requestDto.getTitle());
        entity.setNumbersCount(requestDto.getNumbersCount());
        entity.setMaxNumber(requestDto.getMaxNumber());
        entity.setStatus(DrawStatus.ACTIVE);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return entity;
    }
    
    public DrawResponseDto entityToResponseDto(Draw entity) {
        DrawResponseDto responseDto = new DrawResponseDto();
        responseDto.setId(entity.getId());
        responseDto.setTitle(entity.getTitle());
        responseDto.setStatus(entity.getStatus());
        responseDto.setNumbersCount(entity.getNumbersCount());
        responseDto.setMaxNumber(entity.getMaxNumber());
        responseDto.setCreatedAt(entity.getCreatedAt());
        return responseDto;
    }
}

package ru.mephi.team26.service;

import io.javalin.http.ConflictResponse;
import io.javalin.http.NotFoundResponse;
import lombok.RequiredArgsConstructor;
import ru.mephi.team26.dto.draw.DrawCreateRequestDto;
import ru.mephi.team26.dto.draw.DrawResponseDto;
import ru.mephi.team26.dto.draw.DrawResultResponseDto;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.entity.DrawResult;
import ru.mephi.team26.mapper.impl.DrawMapper;
import ru.mephi.team26.mapper.impl.DrawResultMapper;
import ru.mephi.team26.repository.DrawRepository;
import ru.mephi.team26.repository.DrawResultRepository;
import ru.mephi.team26.validator.impl.DrawValidator;

import java.util.List;

@RequiredArgsConstructor
public class DrawService {
    private final DrawRepository drawRepository;
    private final DrawResultRepository drawResultRepository;
    private final DrawMapper drawMapper;
    private final DrawResultMapper drawResultMapper;
    private final DrawValidator drawValidator;

    public DrawResponseDto createDraw(DrawCreateRequestDto dto) {
        drawValidator.validate(dto);
        Draw draw = drawMapper.requestDtoToEntity(dto);
        drawRepository.save(draw);
        return drawMapper.entityToResponseDto(draw);
    }

    public List<DrawResponseDto> getActiveDraws() {
        List<Draw> activeDraws = drawRepository.findActive();
        return activeDraws.stream().map(drawMapper::entityToResponseDto).toList();
    }

    public DrawResponseDto completeDraw(long drawId) {
        Draw draw = drawRepository.completeById(drawId);
        return drawMapper.entityToResponseDto(draw);
    }

    public DrawResultResponseDto getDrawResultById(long drawId) {
        drawRepository.findById(drawId)
                .orElseThrow(() -> new NotFoundResponse("Draw with id " + drawId + " was not found"));
        DrawResult drawResult = drawResultRepository.findResult(drawId)
                .orElseThrow(() -> new ConflictResponse("Draw with id " + drawId + " is not completed yet"));
        return drawResultMapper.entityToResponseDto(drawResult);
    }
}

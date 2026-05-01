package ru.mephi.team26.service;

import ru.mephi.team26.dto.draw.DrawCreateRequestDto;
import ru.mephi.team26.dto.draw.DrawResponseDto;
import ru.mephi.team26.dto.draw.DrawResultResponseDto;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.entity.DrawResult;
import ru.mephi.team26.entity.DrawStatus;
import ru.mephi.team26.exception.ApiException;
import ru.mephi.team26.mapper.DrawMapper;
import ru.mephi.team26.mapper.DrawResultMapper;
import ru.mephi.team26.repository.DrawRepository;
import ru.mephi.team26.repository.DrawResultRepository;
import ru.mephi.team26.util.GeneratorUtil;
import ru.mephi.team26.validator.DrawValidator;

import java.util.List;

public class DrawService {
    private final DrawRepository drawRepository;
    private final DrawResultRepository drawResultRepository;
    private final DrawMapper drawMapper;
    private final DrawResultMapper drawResultMapper;
    private final DrawValidator drawValidator;

    public DrawService(DrawRepository drawRepository, DrawResultRepository drawResultRepository, DrawMapper drawMapper, DrawResultMapper drawResultMapper, DrawValidator drawValidator) {
        this.drawRepository = drawRepository;
        this.drawResultRepository = drawResultRepository;
        this.drawMapper = drawMapper;
        this.drawResultMapper = drawResultMapper;
        this.drawValidator = drawValidator;
    }

    public DrawResponseDto createDraw(DrawCreateRequestDto dto) {
        drawValidator.validateDraw(dto);
        Draw draw = drawMapper.requestDtoToEntity(dto);
        drawRepository.save(draw);
        return drawMapper.entityToResponseDto(draw);
    }

    public List<DrawResponseDto> getActiveDraws() {
        List<Draw> activeDraws = drawRepository.findActive();
        return activeDraws.stream().map(drawMapper::entityToResponseDto).toList();
    }

    public DrawResponseDto completeDraw(long drawId) {
        Draw draw = drawRepository.findById(drawId).orElseThrow(() -> new ApiException(404, "Draw not found"));
        if (draw.getStatus() != DrawStatus.ACTIVE) {
            throw new ApiException(409, "Draw is already completed");
        }

        List<Integer> winningNumbers = GeneratorUtil.generateWinningNumbers(draw.getNumbersCount(), draw.getMaxNumber());
        drawRepository.completeAndSettle(draw.getId(), winningNumbers);

        Draw completedDraw = drawRepository.findById(draw.getId()).get();
        return drawMapper.entityToResponseDto(completedDraw);
    }

    public DrawResultResponseDto getDrawResultById(long drawId) {
        drawRepository.findById(drawId)
                .orElseThrow(() -> new ApiException(404, "Draw not found"));
        DrawResult drawResult = drawResultRepository.findResult(drawId)
                .orElseThrow(() -> new ApiException(409, "Draw is not completed yet"));
        return drawResultMapper.entityToResponseDto(drawResult);
    }
}

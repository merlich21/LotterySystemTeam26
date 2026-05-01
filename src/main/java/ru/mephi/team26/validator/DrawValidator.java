package ru.mephi.team26.validator;

import ru.mephi.team26.dto.draw.DrawCreateRequestDto;
import ru.mephi.team26.exception.ApiException;

public class DrawValidator {

    public void validateDraw(DrawCreateRequestDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new ApiException(400, "Draw title is required");
        }
        if (dto.getNumbersCount() < 3 || dto.getNumbersCount() > 10) {
            throw new ApiException(400, "numbersCount must be in range [3..10]");
        }
        if (dto.getMaxNumber() < 10 || dto.getMaxNumber() > 99) {
            throw new ApiException(400, "maxNumber must be in range [10..99]");
        }
        if (dto.getNumbersCount() >= dto.getMaxNumber()) {
            throw new ApiException(400, "numbersCount must be less than maxNumber");
        }
    }
}

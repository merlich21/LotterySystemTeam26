package ru.mephi.team26.validator.impl;

import io.javalin.http.BadRequestResponse;
import ru.mephi.team26.dto.draw.DrawCreateRequestDto;
import ru.mephi.team26.validator.Validator;

public class DrawValidator implements Validator<DrawCreateRequestDto> {

    @Override
    public void validate(DrawCreateRequestDto dto, Object... args) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestResponse("Title is required");
        }
        if (dto.getNumbersCount() < 3 || dto.getNumbersCount() > 10) {
            throw new BadRequestResponse("Numbers count must be in range [3..10]");
        }
        if (dto.getMaxNumber() < 10 || dto.getMaxNumber() > 99) {
            throw new BadRequestResponse("Max number must be in range [10..99]");
        }
        if (dto.getNumbersCount() >= dto.getMaxNumber()) {
            throw new BadRequestResponse("Numbers count must be less than max number");
        }
    }
}

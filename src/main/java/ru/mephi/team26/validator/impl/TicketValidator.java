package ru.mephi.team26.validator.impl;

import io.javalin.http.BadRequestResponse;
import ru.mephi.team26.dto.ticket.TicketCreateRequestDto;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.validator.Validator;

import java.util.HashSet;
import java.util.Set;

public class TicketValidator implements Validator<TicketCreateRequestDto> {

    @Override
    public void validate(TicketCreateRequestDto dto, Object... args) {
        Draw draw = (Draw) args[0];
        if (dto.getNumbers() == null || dto.getNumbers().size() != draw.getNumbersCount()) {
            throw new BadRequestResponse("Numbers count must be exactly " + draw.getNumbersCount());
        }

        Set<Integer> unique = new HashSet<>();
        for (Integer number : dto.getNumbers()) {
            if (number == null || number < 1 || number > draw.getMaxNumber()) {
                throw new BadRequestResponse("Each number must be between 1 and " + draw.getMaxNumber());
            }
            if (!unique.add(number)) {
                throw new BadRequestResponse("All numbers must be unique");
            }
        }
    }
}

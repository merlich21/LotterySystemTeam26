package ru.mephi.team26.validator;

import ru.mephi.team26.dto.ticket.TicketCreateRequestDto;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.exception.ApiException;

import java.util.HashSet;
import java.util.Set;

public class TicketValidator {

    public void validateTicket(TicketCreateRequestDto dto, Draw draw) {
        if (dto.getNumbers() == null || dto.getNumbers().size() != draw.getNumbersCount()) {
            throw new ApiException(400, "Numbers count must be exactly " + draw.getNumbersCount());
        }

        Set<Integer> unique = new HashSet<>();
        for (Integer number : dto.getNumbers()) {
            if (number == null || number < 1 || number > draw.getMaxNumber()) {
                throw new ApiException(400, "Each number must be between 1 and " + draw.getMaxNumber());
            }
            if (!unique.add(number)) {
                throw new ApiException(400, "Numbers must be unique");
            }
        }
    }
}

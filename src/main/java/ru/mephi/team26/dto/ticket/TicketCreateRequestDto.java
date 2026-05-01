package ru.mephi.team26.dto.ticket;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TicketCreateRequestDto {
    private long drawId;
    private List<Integer> numbers;
}

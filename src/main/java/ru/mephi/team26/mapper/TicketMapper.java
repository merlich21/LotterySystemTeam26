package ru.mephi.team26.mapper;

import ru.mephi.team26.dto.ticket.TicketCreateRequestDto;
import ru.mephi.team26.dto.ticket.TicketResponseDto;
import ru.mephi.team26.entity.Ticket;
import ru.mephi.team26.entity.TicketStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class TicketMapper {

    public Ticket requestDtoToEntity(TicketCreateRequestDto requestDto) {
        Ticket ticket = new Ticket();
        List<Integer> numbers = requestDto.getNumbers();
        numbers.sort(Integer::compareTo);
        ticket.setNumbers(numbers);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return ticket;
    }

    public TicketResponseDto entityToResponseDto(Ticket ticket) {
        TicketResponseDto responseDto = new TicketResponseDto();
        responseDto.setId(ticket.getId());
        responseDto.setDrawId(ticket.getDraw().getId());
        responseDto.setUserId(ticket.getUser().getId());
        responseDto.setNumbers(ticket.getNumbers());
        responseDto.setStatus(ticket.getStatus());
        responseDto.setCreatedAt(ticket.getCreatedAt());
        return responseDto;
    }
}

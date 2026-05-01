package ru.mephi.team26.service;

import ru.mephi.team26.dto.ticket.TicketCreateRequestDto;
import ru.mephi.team26.dto.ticket.TicketResponseDto;
import ru.mephi.team26.entity.Draw;
import ru.mephi.team26.entity.DrawStatus;
import ru.mephi.team26.entity.Ticket;
import ru.mephi.team26.entity.User;
import ru.mephi.team26.exception.ApiException;
import ru.mephi.team26.mapper.TicketMapper;
import ru.mephi.team26.repository.DrawRepository;
import ru.mephi.team26.repository.TicketRepository;
import ru.mephi.team26.repository.UserRepository;
import ru.mephi.team26.validator.TicketValidator;

public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DrawRepository drawRepository;
    private final TicketMapper ticketMapper;
    private final TicketValidator ticketValidator;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, DrawRepository drawRepository, TicketMapper ticketMapper, TicketValidator ticketValidator) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.drawRepository = drawRepository;
        this.ticketMapper = ticketMapper;
        this.ticketValidator = ticketValidator;
    }

    public TicketResponseDto createTicket(TicketCreateRequestDto dto, long userId) {
        Draw draw = drawRepository.findById(dto.getDrawId()).orElseThrow(() -> new ApiException(404, "Draw not found"));
        if (draw.getStatus() != DrawStatus.ACTIVE) {
            throw new ApiException(409, "Tickets can only be created for an active draw");
        }
        ticketValidator.validateTicket(dto, draw);
        User user = userRepository.findById(userId).get();
        Ticket ticket = ticketMapper.requestDtoToEntity(dto);
        ticket.setDraw(draw);
        ticket.setUser(user);
        ticketRepository.save(ticket);
        return ticketMapper.entityToResponseDto(ticket);
    }

    public TicketResponseDto getTicketById(long ticketId, long userId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new ApiException(404, "Ticket not found"));
        if (ticket.getUser().getId() != userId) {
            throw new ApiException(403, "You can only access your own ticket");
        }
        return ticketMapper.entityToResponseDto(ticket);
    }
}

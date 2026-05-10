package ru.mephi.team26.service;

import io.javalin.http.ConflictResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import lombok.RequiredArgsConstructor;
import ru.mephi.team26.dto.ticket.TicketCreateRequestDto;
import ru.mephi.team26.dto.ticket.TicketResponseDto;
import ru.mephi.team26.entity.*;
import ru.mephi.team26.mapper.impl.TicketMapper;
import ru.mephi.team26.repository.DrawRepository;
import ru.mephi.team26.repository.TicketRepository;
import ru.mephi.team26.repository.UserRepository;
import ru.mephi.team26.validator.impl.TicketValidator;

import java.util.List;

@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DrawRepository drawRepository;
    private final TicketMapper ticketMapper;
    private final TicketValidator ticketValidator;

    public TicketResponseDto createTicket(TicketCreateRequestDto dto, long userId) {
        Draw draw = drawRepository.findById(dto.getDrawId())
                .orElseThrow(() -> new NotFoundResponse("Draw with id " + dto.getDrawId() + " was not found"));

        if (draw.getStatus() != DrawStatus.ACTIVE) {
            throw new ConflictResponse("Draw with id " + dto.getDrawId() + " is already completed. " +
                                       "Tickets can only be created for an active draw");
        }
        ticketValidator.validate(dto, draw);

        User user = userRepository.findById(userId);

        Ticket ticket = ticketMapper.requestDtoToEntity(dto);
        ticket.setDraw(draw);
        ticket.setUser(user);
        ticketRepository.save(ticket);

        return ticketMapper.entityToResponseDto(ticket);
    }

    public TicketResponseDto getTicketById(long ticketId, long userId, Role role) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundResponse("Ticket with id " + ticketId + " was not found"));

        if (role != Role.ADMIN && ticket.getUser().getId() != userId) {
            throw new ForbiddenResponse("Users can only access their own tickets");
        }
        return ticketMapper.entityToResponseDto(ticket);
    }

    public List<TicketResponseDto> getTicketsByDrawIdAndStatus(long drawId, TicketStatus status) {
        Draw draw = drawRepository.findById(drawId)
                .orElseThrow(() -> new NotFoundResponse("Draw with id " + drawId + " was not found"));
        List<Ticket> tickets = ticketRepository.findAllByDrawAndStatus(draw, status);
        return tickets.stream().map(ticketMapper::entityToResponseDto).toList();
    }
}

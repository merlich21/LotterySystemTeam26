package ru.mephi.team26.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import ru.mephi.team26.dto.ticket.TicketCreateRequestDto;
import ru.mephi.team26.dto.ticket.TicketResponseDto;
import ru.mephi.team26.entity.Role;
import ru.mephi.team26.service.TicketService;

@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    public void init(Javalin app) {
        app.post("/api/tickets", this::createTicket, Role.USER);
        app.get("/api/tickets/{ticketId}/result", this::getTicketResult, Role.USER, Role.ADMIN);
    }

    private void createTicket(Context ctx) {
        TicketCreateRequestDto requestDto = ctx.bodyAsClass(TicketCreateRequestDto.class);
        Long userId = ctx.attribute("userId");
        TicketResponseDto responseDto = ticketService.createTicket(requestDto, userId);
        ctx.status(201).json(responseDto);
    }

    private void getTicketResult(Context ctx) {
        long ticketId = Long.parseLong(ctx.pathParam("ticketId"));
        Long userId = ctx.attribute("userId");
        Role role = Role.valueOf(ctx.attribute("role"));
        TicketResponseDto responseDto = ticketService.getTicketById(ticketId, userId, role);
        ctx.status(200).json(responseDto);
    }
}

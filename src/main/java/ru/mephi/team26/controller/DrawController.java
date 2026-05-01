package ru.mephi.team26.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import ru.mephi.team26.dto.draw.DrawCreateRequestDto;
import ru.mephi.team26.dto.draw.DrawResponseDto;
import ru.mephi.team26.dto.draw.DrawResultResponseDto;
import ru.mephi.team26.entity.Role;
import ru.mephi.team26.service.DrawService;

import java.util.List;

public class DrawController {
    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    public void init(Javalin app) {
        app.post("/api/draws", this::createDraw, Role.ADMIN);
        app.get("/api/draws/active", this::getActiveDraws, Role.USER, Role.ADMIN);
        app.post("/api/draws/{drawId}/complete", this::completeDraw, Role.ADMIN);
        app.get("/api/draws/{drawId}/result", this::getDrawResult, Role.USER, Role.ADMIN);
    }

    private void createDraw(Context ctx) {
        DrawCreateRequestDto requestDto = ctx.bodyAsClass(DrawCreateRequestDto.class);
        DrawResponseDto responseDto = drawService.createDraw(requestDto);
        ctx.status(201).json(responseDto);
    }

    private void getActiveDraws(Context ctx) {
        List<DrawResponseDto> responseDtos = drawService.getActiveDraws();
        ctx.status(200).json(responseDtos);
    }

    private void completeDraw(Context ctx) {
        long drawId = Long.parseLong(ctx.pathParam("drawId"));
        DrawResponseDto responseDto = drawService.completeDraw(drawId);
        ctx.status(200).json(responseDto);
    }

    private void getDrawResult(Context ctx) {
        long drawId = Long.parseLong(ctx.pathParam("drawId"));
        DrawResultResponseDto responseDto = drawService.getDrawResultById(drawId);
        ctx.status(200).json(responseDto);
    }
}

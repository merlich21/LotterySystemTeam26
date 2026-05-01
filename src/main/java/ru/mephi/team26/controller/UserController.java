package ru.mephi.team26.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import ru.mephi.team26.dto.auth.AuthRequestDto;
import ru.mephi.team26.dto.auth.AuthResponseDto;
import ru.mephi.team26.service.UserService;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void init(Javalin app) {
        app.post("/api/auth/register", this::register);
        app.post("/api/auth/login", this::login);
    }

    private void register(Context ctx) {
        AuthRequestDto requestDto = ctx.bodyAsClass(AuthRequestDto.class);
        AuthResponseDto responseDto = userService.register(requestDto);
        ctx.status(201).json(responseDto);
    }

    private void login(Context ctx) {
        AuthRequestDto requestDto = ctx.bodyAsClass(AuthRequestDto.class);
        AuthResponseDto responseDto = userService.login(requestDto);
        ctx.status(200).json(responseDto);
    }
}

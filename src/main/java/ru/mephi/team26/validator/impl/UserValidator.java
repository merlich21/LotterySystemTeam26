package ru.mephi.team26.validator.impl;

import io.javalin.http.BadRequestResponse;
import ru.mephi.team26.dto.auth.AuthRequestDto;
import ru.mephi.team26.validator.Validator;

public class UserValidator implements Validator<AuthRequestDto> {

    @Override
    public void validate(AuthRequestDto dto, Object... args) {
        if (dto.getUsername() == null || dto.getUsername().isBlank() || dto.getUsername().length() < 3) {
            throw new BadRequestResponse("Username must not be empty and have at least 3 characters");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new BadRequestResponse("Password must not be empty and have at least 6 characters");
        }
    }
}

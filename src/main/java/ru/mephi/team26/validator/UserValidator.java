package ru.mephi.team26.validator;

import ru.mephi.team26.dto.auth.AuthRequestDto;
import ru.mephi.team26.exception.ApiException;

public class UserValidator {

    public void validateCredentials(AuthRequestDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank() || dto.getUsername().length() < 3) {
            throw new ApiException(400, "Username must be at least 3 characters");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new ApiException(400, "Password must be at least 6 characters");
        }
    }
}

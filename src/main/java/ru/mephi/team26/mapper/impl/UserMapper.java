package ru.mephi.team26.mapper.impl;

import ru.mephi.team26.dto.auth.AuthRequestDto;
import ru.mephi.team26.dto.auth.AuthResponseDto;
import ru.mephi.team26.entity.Role;
import ru.mephi.team26.entity.User;
import ru.mephi.team26.mapper.RequestMapper;
import ru.mephi.team26.mapper.ResponseMapper;
import ru.mephi.team26.security.JwtProvider;
import ru.mephi.team26.util.PasswordUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class UserMapper implements RequestMapper<User, AuthRequestDto>, ResponseMapper<User, AuthResponseDto> {
    private final JwtProvider jwtProvider;

    public UserMapper(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public User requestDtoToEntity(AuthRequestDto requestDto) {
        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setPasswordHash(PasswordUtil.hashPassword(requestDto.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return user;
    }

    @Override
    public AuthResponseDto entityToResponseDto(User user) {
        AuthResponseDto responseDto = new AuthResponseDto();
        responseDto.setId(user.getId());
        responseDto.setUsername(user.getUsername());
        responseDto.setRole(user.getRole());
        responseDto.setJwt(jwtProvider.createToken(user));
        return responseDto;
    }
}

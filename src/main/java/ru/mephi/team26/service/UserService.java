package ru.mephi.team26.service;

import io.javalin.http.ConflictResponse;
import io.javalin.http.UnauthorizedResponse;
import lombok.RequiredArgsConstructor;
import ru.mephi.team26.dto.auth.AuthRequestDto;
import ru.mephi.team26.dto.auth.AuthResponseDto;
import ru.mephi.team26.entity.Role;
import ru.mephi.team26.entity.User;
import ru.mephi.team26.mapper.impl.UserMapper;
import ru.mephi.team26.repository.UserRepository;
import ru.mephi.team26.util.PasswordUtil;
import ru.mephi.team26.validator.impl.UserValidator;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(PasswordUtil.hashPassword("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPasswordHash(PasswordUtil.hashPassword("user123"));
            user.setRole(Role.USER);
            user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userRepository.save(user);
        }
    }

    public AuthResponseDto register(AuthRequestDto dto) {
        userValidator.validate(dto);

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictResponse("User with that username already exists");
        }

        User user = userMapper.requestDtoToEntity(dto);
        userRepository.save(user);
        return userMapper.entityToResponseDto(user);
    }

    public AuthResponseDto login(AuthRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UnauthorizedResponse("Invalid credentials"));

        if (!user.getPasswordHash().equals(PasswordUtil.hashPassword(dto.getPassword()))) {
            throw new UnauthorizedResponse("Invalid credentials");
        }

        return userMapper.entityToResponseDto(user);
    }
}

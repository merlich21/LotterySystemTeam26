package ru.mephi.team26.service;

import ru.mephi.team26.dto.auth.AuthRequestDto;
import ru.mephi.team26.dto.auth.AuthResponseDto;
import ru.mephi.team26.entity.User;
import ru.mephi.team26.exception.ApiException;
import ru.mephi.team26.mapper.UserMapper;
import ru.mephi.team26.repository.UserRepository;
import ru.mephi.team26.util.PasswordUtil;
import ru.mephi.team26.validator.UserValidator;

public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    public UserService(UserRepository userRepository, UserMapper userMapper, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
    }

    public void init() {
        //userRepository.createIfAbsent("admin", PasswordHasher.sha256("admin123"), Role.ADMIN);
        //userRepository.createIfAbsent("user", PasswordHasher.sha256("user123"), Role.USER);
    }

    public AuthResponseDto register(AuthRequestDto dto) {
        userValidator.validateCredentials(dto);
        userRepository.findByUsername(dto.getUsername()).ifPresent(user -> {
            throw new ApiException(409, "User with that username already exists");
        });

        User user = userMapper.requestDtoToEntity(dto);
        userRepository.save(user);
        return userMapper.entityToResponseDto(user);
    }

    public AuthResponseDto login(AuthRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ApiException(401, "Invalid credentials"));

        if (!user.getPasswordHash().equals(PasswordUtil.hashPassword(dto.getPassword()))) {
            throw new ApiException(401, "Invalid credentials");
        }
        return userMapper.entityToResponseDto(user);
    }
}

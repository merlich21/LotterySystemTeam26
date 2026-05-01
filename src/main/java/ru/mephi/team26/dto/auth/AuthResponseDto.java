package ru.mephi.team26.dto.auth;

import lombok.Getter;
import lombok.Setter;
import ru.mephi.team26.entity.Role;

@Getter
@Setter
public class AuthResponseDto {
    private long id;
    private String username;
    private Role role;
    private String jwt;
}

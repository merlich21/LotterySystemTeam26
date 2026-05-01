package ru.mephi.team26.entity;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    USER,
    ADMIN
}

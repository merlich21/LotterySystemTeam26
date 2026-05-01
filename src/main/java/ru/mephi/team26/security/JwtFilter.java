package ru.mephi.team26.security;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import io.jsonwebtoken.Claims;
import ru.mephi.team26.entity.Role;

import java.util.Set;

public class JwtFilter {
    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public void init(Javalin app) {
        app.beforeMatched(this::handle);
    }

    private void handle(Context ctx) {
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        if (permittedRoles.isEmpty()) {
            return;
        }

        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Token missing or invalid");
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = jwtProvider.parseToken(token);

            ctx.attribute("username", claims.getSubject());
            ctx.attribute("userId", claims.get("userId", Long.class));

            String role = claims.get("role", String.class);
            ctx.attribute("role", role);

            if (!permittedRoles.contains(Role.valueOf(role))) {
                throw new ForbiddenResponse("Insufficient permissions");
            }
        } catch (Exception e) {
            throw new UnauthorizedResponse("Invalid token: " + e.getMessage());
        }
    }
}
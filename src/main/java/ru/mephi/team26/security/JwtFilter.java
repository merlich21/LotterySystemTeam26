package ru.mephi.team26.security;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import ru.mephi.team26.entity.Role;

import java.util.Set;

@RequiredArgsConstructor
public class JwtFilter {
    private final JwtProvider jwtProvider;

    public void init(Javalin app) {
        app.beforeMatched(this::filter);
    }

    private void filter(Context ctx) {
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        if (permittedRoles.isEmpty()) {
            return;
        }

        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Missing JWT");
        }

        try {
            String jwt = authHeader.replace("Bearer ", "");
            Claims claims = jwtProvider.parseToken(jwt);

            ctx.attribute("username", claims.getSubject());
            ctx.attribute("userId", claims.get("userId", Long.class));

            String role = claims.get("role", String.class);
            ctx.attribute("role", role);

            if (!permittedRoles.contains(Role.valueOf(role))) {
                throw new ForbiddenResponse("Insufficient permissions");
            }
        } catch (Exception e) {
            throw new UnauthorizedResponse("Invalid JWT: " + e.getMessage());
        }
    }
}
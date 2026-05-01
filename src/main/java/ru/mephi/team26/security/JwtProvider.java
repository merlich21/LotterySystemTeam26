package ru.mephi.team26.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ru.mephi.team26.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtProvider {
    private final SecretKey key;

    public JwtProvider() {
        this.key = Keys.hmacShaKeyFor("secretsecretsecretsecretsecretsecret".getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

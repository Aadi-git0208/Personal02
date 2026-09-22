package com.apexcare.order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final String secret;
    private SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret;
    }

    @PostConstruct
    void validateSecret() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public AuthenticatedUser parseUser(String token) {
        Claims claims = parseClaims(token);
        Long userId = toLong(claims.get("userId"));
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        if (userId == null || email == null || email.isBlank() || role == null || role.isBlank()) {
            throw new JwtException("Token is missing required identity claims");
        }
        return new AuthenticatedUser(userId, email, role);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }
}

package com.medicurex.appointment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@Service
public class JwtService {

    private static final Set<String> ALLOWED_ROLES = Set.of("PATIENT", "DOCTOR", "ADMIN");

    private final String secret;
    private SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret;
    }

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be set and must match the Identity/Auth service");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public AuthenticatedUser parseUser(String token) {
        Claims claims = parseClaims(token);
        Long userId = extractUserId(claims);
        String email = claims.getSubject();
        String role = extractRole(claims);

        if (userId == null || email == null || email.isBlank() || role == null) {
            throw new JwtException("Token is missing required claims");
        }

        return new AuthenticatedUser(userId, email.trim().toLowerCase(Locale.ROOT), role);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Long extractUserId(Claims claims) {
        Object raw = claims.get("userId");
        if (raw instanceof Number number) {
            return number.longValue();
        }
        if (raw instanceof String value && !value.isBlank()) {
            return Long.parseLong(value);
        }
        return null;
    }

    private String extractRole(Claims claims) {
        Object raw = claims.get("role");
        if (raw == null) {
            return null;
        }

        String role = raw.toString().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(role)) {
            throw new JwtException("Token contains an invalid role");
        }
        return role;
    }
}

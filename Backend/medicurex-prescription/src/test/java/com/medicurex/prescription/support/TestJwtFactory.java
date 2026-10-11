package com.medicurex.prescription.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public final class TestJwtFactory {

    public static final String SECRET = "TestOnlySecretKeyThatIsLongEnoughForHS256Algorithms!!";

    private TestJwtFactory() {
    }

    public static String token(long userId, String email, String role) {
        return token(userId, email, role, Instant.now().plusSeconds(3600));
    }

    public static String expiredToken(long userId, String email, String role) {
        return token(userId, email, role, Instant.now().minusSeconds(60));
    }

    public static String token(long userId, String email, String role, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant issuedAt = expiration.isBefore(Instant.now()) ? expiration.minusSeconds(60) : Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }
}

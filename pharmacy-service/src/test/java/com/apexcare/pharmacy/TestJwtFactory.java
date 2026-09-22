package com.apexcare.pharmacy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class TestJwtFactory {

    public static final String SECRET = "TestOnlySecretKeyThatIsLongEnoughForHS256Algorithms!!";

    private TestJwtFactory() {
    }

    public static String token(long userId, String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + 3_600_000))
                .signWith(key)
                .compact();
    }
}

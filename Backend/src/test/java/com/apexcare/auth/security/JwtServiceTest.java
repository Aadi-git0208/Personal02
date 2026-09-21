package com.apexcare.auth.security;

import com.apexcare.auth.model.Role;
import com.apexcare.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("TestOnlySecretKeyThatIsLongEnoughForHS256Algorithms!!", 3_600_000);
        jwtService.validateSecret();
    }

    @Test
    void generateTokenContainsExpectedClaims() {
        User user = new User();
        user.setId(42L);
        user.setEmail("rahul@gmail.com");
        user.setRole(Role.PATIENT);

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertThat(token).isNotBlank();
        assertThat(claims.getSubject()).isEqualTo("rahul@gmail.com");
        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("patient");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService shortLivedService = new JwtService(
                "TestOnlySecretKeyThatIsLongEnoughForHS256Algorithms!!",
                1
        );
        shortLivedService.validateSecret();

        User user = new User();
        user.setId(1L);
        user.setEmail("rahul@gmail.com");
        user.setRole(Role.PATIENT);

        String token = shortLivedService.generateToken(user);
        Thread.sleep(20);

        assertThatThrownBy(() -> shortLivedService.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}

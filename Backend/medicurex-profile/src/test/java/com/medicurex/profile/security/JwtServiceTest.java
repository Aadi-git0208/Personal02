package com.medicurex.profile.security;

import com.medicurex.profile.support.TestJwtFactory;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TestJwtFactory.SECRET);
        jwtService.validateSecret();
    }

    @Test
    void parsesCompatibleAuthTokenClaims() {
        String token = TestJwtFactory.token(42L, "rahul@gmail.com", "patient");
        AuthenticatedUser user = jwtService.parseUser(token);

        assertThat(user.getUserId()).isEqualTo(42L);
        assertThat(user.getEmail()).isEqualTo("rahul@gmail.com");
        assertThat(user.getRole()).isEqualTo("PATIENT");
        assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_PATIENT");
    }

    @Test
    void rejectsExpiredToken() {
        String token = TestJwtFactory.expiredToken(42L, "rahul@gmail.com", "patient");
        assertThatThrownBy(() -> jwtService.parseUser(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsTokenWithInvalidRole() {
        String token = TestJwtFactory.token(42L, "rahul@gmail.com", "superuser");
        assertThatThrownBy(() -> jwtService.parseUser(token))
                .isInstanceOf(JwtException.class);
    }
}

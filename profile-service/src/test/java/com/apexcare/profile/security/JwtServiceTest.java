package com.apexcare.profile.security;

import com.apexcare.profile.TestJwtFactory;
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
    void parseUserReadsIdentityClaims() {
        String token = TestJwtFactory.token(42L, "rahul@gmail.com", "patient");

        AuthenticatedUser user = jwtService.parseUser(token);

        assertThat(user.getUserId()).isEqualTo(42L);
        assertThat(user.getEmail()).isEqualTo("rahul@gmail.com");
        assertThat(user.getRole()).isEqualTo("PATIENT");
        assertThat(user.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_PATIENT");
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        String token = TestJwtFactory.token(1L, "rahul@gmail.com", "patient", 1);
        Thread.sleep(20);

        assertThatThrownBy(() -> jwtService.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = TestJwtFactory.token(1L, "rahul@gmail.com", "patient");
        JwtService other = new JwtService("AnotherSecretKeyThatIsLongEnoughForHS256Algorithms!!");
        other.validateSecret();

        assertThatThrownBy(() -> other.parseUser(token))
                .isInstanceOf(JwtException.class);
    }
}

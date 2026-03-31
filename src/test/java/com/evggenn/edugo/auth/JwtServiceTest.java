package com.evggenn.edugo.auth;

import com.evggenn.edugo.user.CustomUserDetails;
import com.evggenn.edugo.user.Role;
import com.evggenn.edugo.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String TEST_SECRET = "test-secret-key-must-be-at-least-32-chars";
    private static final long TEST_EXPIRATION_MS = 86400000L;  // 24 hrs

    @BeforeEach
    public void setupInit() {
        jwtService = new JwtService(TEST_EXPIRATION_MS, TEST_SECRET);
        userDetails = new CustomUserDetails(
                User.builder()
                        .id(1L)
                        .email("test@test.com")
                        .password("hash_password")
                        .firstName("Vasia")
                        .lastName("Vasilkov")
                        .middleName("Zagagulivich")
                        .roles(Set.of(new Role(1L, Role.STUDENT)))
                        .build()
        );
    }

    @Test
    public void generateToken_shouldReturnNonEmptyToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotEmpty();
    }

    @Test
    public void generateToken_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@test.com");
    }

    @Test
    public void isValid_shouldReturnTrue_whenTokenValid() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    public void isValid_shouldReturnFalse_whenTokenExpired() {
        JwtService expiredJwtService = new JwtService(-1000L, TEST_SECRET);
        String token = expiredJwtService.generateToken(userDetails);
        assertThat(expiredJwtService.isValid(token)).isFalse();
    }

    @Test
    public void isValid_shouldReturnFalse_whenTokenNotValid() {
        assertThat(jwtService.isValid("not-valid-token")).isFalse();
    }

    @Test
    public void generateToken_shouldContainRoles() {
        String token = jwtService.generateToken(userDetails);
        List<String> roles = jwtService.extractRoles(token);
        assertThat(roles).containsExactlyInAnyOrder(Role.STUDENT);
    }
}
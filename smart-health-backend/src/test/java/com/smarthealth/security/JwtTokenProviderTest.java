package com.smarthealth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "this-is-a-test-secret-key-that-is-at-least-32-bytes-long-for-hmac",
                3600000L);
    }

    @Test
    void generateToken_shouldProduceValidToken() {
        String token = jwtTokenProvider.generateToken(1L, "testuser", "USER");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectId() {
        String token = jwtTokenProvider.generateToken(42L, "testuser", "USER");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(42L, userId);
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtTokenProvider.generateToken(1L, "testuser", "USER");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtTokenProvider.generateToken(1L, "admin", "ADMIN");
        String role = jwtTokenProvider.getRoleFromToken(token);
        assertEquals("ADMIN", role);
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtTokenProvider.generateToken(1L, "testuser", "USER");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }
}

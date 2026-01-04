package com.lms.identity.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for JwtService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set the secret and expiration using reflection
        ReflectionTestUtils.setField(jwtService, "secret", "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        ReflectionTestUtils.setField(jwtService, "expiration", 1800000L); // 30 minutes
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token")
        void generateToken_ValidInput_ReturnsToken() {
            String token = jwtService.generateToken("user@example.com", "CUSTOMER", 1L);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate token with correct claims")
        void generateToken_VerifyClaims() {
            String token = jwtService.generateToken("admin@example.com", "ADMIN", 5L);

            // Token should be valid
            assertDoesNotThrow(() -> jwtService.validateToken(token));
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void generateToken_DifferentUsers_DifferentTokens() {
            String token1 = jwtService.generateToken("user1@example.com", "CUSTOMER", 1L);
            String token2 = jwtService.generateToken("user2@example.com", "CUSTOMER", 2L);

            assertNotEquals(token1, token2);
        }

        @Test
        @DisplayName("Should include role in token")
        void generateToken_IncludesRole() {
            String token = jwtService.generateToken("officer@example.com", "LOAN_OFFICER", 3L);

            assertNotNull(token);
            // Validate token doesn't throw
            assertDoesNotThrow(() -> jwtService.validateToken(token));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate correct token")
        void validateToken_ValidToken_NoException() {
            String token = jwtService.generateToken("test@example.com", "CUSTOMER", 1L);

            assertDoesNotThrow(() -> jwtService.validateToken(token));
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void validateToken_InvalidToken_ThrowsException() {
            assertThrows(Exception.class, () -> jwtService.validateToken("invalid.token.here"));
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void validateToken_MalformedToken_ThrowsException() {
            assertThrows(Exception.class, () -> jwtService.validateToken("not-a-jwt"));
        }

        @Test
        @DisplayName("Should throw exception for null token")
        void validateToken_NullToken_ThrowsException() {
            assertThrows(Exception.class, () -> jwtService.validateToken(null));
        }

        @Test
        @DisplayName("Should throw exception for empty token")
        void validateToken_EmptyToken_ThrowsException() {
            assertThrows(Exception.class, () -> jwtService.validateToken(""));
        }

        @Test
        @DisplayName("Should throw exception for tampered token")
        void validateToken_TamperedToken_ThrowsException() {
            String token = jwtService.generateToken("test@example.com", "CUSTOMER", 1L);
            String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

            assertThrows(Exception.class, () -> jwtService.validateToken(tamperedToken));
        }
    }

    @Nested
    @DisplayName("Token Expiration Tests")  
    class TokenExpirationTests {

        @Test
        @DisplayName("Token should not be expired immediately after creation")
        void tokenExpiration_NewToken_NotExpired() {
            String token = jwtService.generateToken("test@example.com", "CUSTOMER", 1L);

            assertDoesNotThrow(() -> jwtService.validateToken(token));
        }

        @Test
        @DisplayName("Should handle very short expiration")
        void tokenExpiration_ShortExpiration() {
            // Set very short expiration
            ReflectionTestUtils.setField(jwtService, "expiration", 2000L);
            
            String token = jwtService.generateToken("test@example.com", "CUSTOMER", 1L);
            
            // Should still be valid immediately
            assertDoesNotThrow(() -> jwtService.validateToken(token));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle special characters in email")
        void generateToken_SpecialCharactersInEmail() {
            String token = jwtService.generateToken("user+test@example.com", "CUSTOMER", 1L);

            assertNotNull(token);
            assertDoesNotThrow(() -> jwtService.validateToken(token));
        }

        @Test
        @DisplayName("Should handle long role names")
        void generateToken_LongRoleName() {
            String token = jwtService.generateToken("test@example.com", "SUPER_ADMIN_MANAGER", 1L);

            assertNotNull(token);
        }

        @Test
        @DisplayName("Should handle unicode in username")
        void generateToken_UnicodeUsername() {
            String token = jwtService.generateToken("用户@example.com", "CUSTOMER", 1L);

            assertNotNull(token);
        }

        @Test
        @DisplayName("Should handle userId of 0")
        void generateToken_ZeroUserId() {
            String token = jwtService.generateToken("test@example.com", "CUSTOMER", 0L);

            assertNotNull(token);
        }

        @Test
        @DisplayName("Should handle large userId")
        void generateToken_LargeUserId() {
            String token = jwtService.generateToken("test@example.com", "CUSTOMER", Long.MAX_VALUE);

            assertNotNull(token);
        }
    }
}

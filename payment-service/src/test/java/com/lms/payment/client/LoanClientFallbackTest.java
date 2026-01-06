package com.lms.payment.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoanClientFallback Tests")
class LoanClientFallbackTest {

    private LoanClientFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new LoanClientFallback();
    }

    @Test
    @DisplayName("debitWallet should throw RuntimeException when circuit breaker is open")
    void debitWallet_ShouldThrowRuntimeException() {
        Long userId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fallback.debitWallet(userId, amount);
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Wallet service temporarily unavailable"));
    }

    @Test
    @DisplayName("creditWallet should throw RuntimeException when circuit breaker is open")
    void creditWallet_ShouldThrowRuntimeException() {
        Long userId = 456L;
        BigDecimal amount = new BigDecimal("1000.00");
        String description = "Test credit";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fallback.creditWallet(userId, amount, description);
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Wallet service temporarily unavailable"));
    }

    @Test
    @DisplayName("debitWallet should log error message")
    void debitWallet_ShouldThrowWithCorrectMessage() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fallback.debitWallet(1L, BigDecimal.TEN);
        });

        assertEquals("Wallet service temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    @DisplayName("creditWallet should log error message")
    void creditWallet_ShouldThrowWithCorrectMessage() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fallback.creditWallet(1L, BigDecimal.TEN, "desc");
        });

        assertEquals("Wallet service temporarily unavailable. Please try again later.", exception.getMessage());
    }
}

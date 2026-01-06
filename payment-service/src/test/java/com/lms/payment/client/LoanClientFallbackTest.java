package com.lms.payment.client;

import com.lms.payment.exception.ServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoanClientFallback Tests")
class LoanClientFallbackTest {

    @Test
    @DisplayName("debitWallet should throw ServiceUnavailableException when circuit breaker is open")
    void debitWalletShouldThrowServiceUnavailableException() {
        LoanClientFallback fallback = new LoanClientFallback();
        Long userId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> fallback.debitWallet(userId, amount)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Wallet service temporarily unavailable"));
    }

    @Test
    @DisplayName("creditWallet should throw ServiceUnavailableException when circuit breaker is open")
    void creditWalletShouldThrowServiceUnavailableException() {
        LoanClientFallback fallback = new LoanClientFallback();
        Long userId = 456L;
        BigDecimal amount = new BigDecimal("1000.00");
        String description = "Test credit";

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> fallback.creditWallet(userId, amount, description)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Wallet service temporarily unavailable"));
    }

    @Test
    @DisplayName("debitWallet should throw with correct message")
    void debitWalletShouldThrowWithCorrectMessage() {
        LoanClientFallback fallback = new LoanClientFallback();

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> fallback.debitWallet(1L, BigDecimal.TEN)
        );

        assertEquals("Wallet service temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    @DisplayName("creditWallet should throw with correct message")
    void creditWalletShouldThrowWithCorrectMessage() {
        LoanClientFallback fallback = new LoanClientFallback();

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> fallback.creditWallet(1L, BigDecimal.TEN, "desc")
        );

        assertEquals("Wallet service temporarily unavailable. Please try again later.", exception.getMessage());
    }
}


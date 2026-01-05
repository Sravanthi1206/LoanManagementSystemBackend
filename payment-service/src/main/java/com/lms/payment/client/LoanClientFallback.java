package com.lms.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Fallback implementation for LoanClient when loan-service is unavailable.
 */
@Slf4j
@Component
public class LoanClientFallback implements LoanClient {

    @Override
    public void debitWallet(Long userId, BigDecimal amount) {
        log.error("Circuit Breaker: Loan Service unavailable. Failed to debit wallet for user {}, amount {}", userId, amount);
        throw new RuntimeException("Wallet service temporarily unavailable. Please try again later.");
    }
}

package com.lms.loan.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback implementation for EmiClient when EMI service is unavailable.
 * Uses circuit breaker pattern to fail gracefully.
 */
@Slf4j
@Component
public class EmiClientFallback implements EmiClient {

    @Override
    public void generateSchedule(Long loanId, Long userId, BigDecimal amount, BigDecimal rate, Integer tenure) {
        log.warn("Circuit Breaker: EMI Service unavailable. EMI schedule generation for Loan {} will be retried later.", loanId);
        // Silently fail - EMI generation can be triggered manually later
        // or through a scheduled job that retries failed EMI generations
    }
}

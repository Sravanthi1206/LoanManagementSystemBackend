package com.lms.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for EmiClient when emi-service is unavailable.
 */
@Slf4j
@Component
public class EmiClientFallback implements EmiClient {

    @Override
    public void markInstallmentAsPaid(Long id) {
        log.error("Circuit Breaker: EMI Service unavailable. Failed to mark installment {} as paid", id);
        throw new RuntimeException("EMI service temporarily unavailable. Please try again later.");
    }
}

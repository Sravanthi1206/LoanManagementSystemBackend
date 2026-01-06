package com.lms.payment.gateway;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Result of a payment gateway operation.
 */
@Data
@Builder
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String gatewayTransactionId;
    private String status;
    private String message;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime processedAt;
    private String errorCode;
    private String errorMessage;
}

package com.lms.payment.gateway;

import java.math.BigDecimal;

/**
 * Interface for payment gateway operations.
 * Implementations can be mock (for testing/demo) or real (Razorpay, Stripe, etc.)
 */
public interface PaymentGateway {

    /**
     * Process a payment through the gateway.
     * @param amount Amount to charge
     * @param currency Currency code (e.g., "INR")
     * @param description Payment description
     * @param customerId Customer identifier
     * @return PaymentResult with transaction details
     */
    PaymentResult processPayment(BigDecimal amount, String currency, String description, String customerId);

    /**
     * Initiate a refund for a previous transaction.
     * @param transactionId Original transaction ID
     * @param amount Amount to refund
     * @return PaymentResult with refund details
     */
    PaymentResult initiateRefund(String transactionId, BigDecimal amount);

    /**
     * Get the status of a transaction.
     * @param transactionId Transaction ID to check
     * @return PaymentResult with current status
     */
    PaymentResult getTransactionStatus(String transactionId);

    /**
     * Get the gateway name.
     */
    String getGatewayName();
}

package com.lms.payment.exception;

/**
 * Exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found with id: " + paymentId);
    }
    
    public PaymentNotFoundException(String transactionId) {
        super("Payment not found with transaction id: " + transactionId);
    }
}

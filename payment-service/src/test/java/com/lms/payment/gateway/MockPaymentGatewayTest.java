package com.lms.payment.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mock Payment Gateway Tests")
class MockPaymentGatewayTest {

    private MockPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new MockPaymentGateway();
    }

    @Test
    @DisplayName("Get Gateway Name")
    void getGatewayName() {
        assertNotNull(gateway.getGatewayName());
        assertTrue(gateway.getGatewayName().contains("Mock"));
    }

    @Test
    @DisplayName("Process Payment - returns result with transaction ID")
    void processPayment_ReturnsResult() {
        PaymentResult result = gateway.processPayment(
                new BigDecimal("1000.00"),
                "INR",
                "Test payment",
                "customer-123"
        );

        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getGatewayTransactionId());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals("INR", result.getCurrency());
        assertNotNull(result.getProcessedAt());
    }

    @Test
    @DisplayName("Process Payment - transaction ID format starts with MPAY")
    void processPayment_TransactionIdFormat() {
        PaymentResult result = gateway.processPayment(
                new BigDecimal("500.00"),
                "INR",
                "Test",
                "user-1"
        );

        // Transaction ID starts with MPAY
        assertTrue(result.getTransactionId().startsWith("MPAY"));
        // Gateway transaction ID has MOCK_ prefix
        assertTrue(result.getGatewayTransactionId().startsWith("MOCK_MPAY"));
    }

    @Test
    @DisplayName("Get Transaction Status - found")
    void getTransactionStatus_Found() {
        // First process a payment to get a transaction ID
        PaymentResult paymentResult = gateway.processPayment(
                new BigDecimal("1000.00"),
                "INR",
                "Test",
                "user-1"
        );

        // Now check status
        PaymentResult statusResult = gateway.getTransactionStatus(paymentResult.getTransactionId());

        assertNotNull(statusResult);
        assertEquals(paymentResult.getTransactionId(), statusResult.getTransactionId());
    }

    @Test
    @DisplayName("Get Transaction Status - not found")
    void getTransactionStatus_NotFound() {
        PaymentResult result = gateway.getTransactionStatus("INVALID-TXN-ID");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("NOT_FOUND", result.getStatus());
        assertEquals("TXN_NOT_FOUND", result.getErrorCode());
    }

    @Test
    @DisplayName("Initiate Refund - success for existing transaction")
    void initiateRefund_Success() {
        // First process a payment
        PaymentResult paymentResult = gateway.processPayment(
                new BigDecimal("1000.00"),
                "INR",
                "Test",
                "user-1"
        );

        // Refund - mock always succeeds for existing transactions
        PaymentResult refundResult = gateway.initiateRefund(
                paymentResult.getTransactionId(),
                new BigDecimal("500.00")
        );

        assertNotNull(refundResult);
        assertTrue(refundResult.isSuccess());
        assertEquals("REFUNDED", refundResult.getStatus());
        assertEquals(new BigDecimal("500.00"), refundResult.getAmount());
    }

    @Test
    @DisplayName("Initiate Refund - not found")
    void initiateRefund_NotFound() {
        PaymentResult result = gateway.initiateRefund(
                "INVALID-TXN",
                new BigDecimal("100.00")
        );

        assertFalse(result.isSuccess());
        assertEquals("TXN_NOT_FOUND", result.getErrorCode());
    }

    @Test
    @DisplayName("Multiple Payments - each gets unique transaction ID")
    void multiplePayments_UniqueIds() {
        PaymentResult result1 = gateway.processPayment(new BigDecimal("100"), "INR", "Test1", "u1");
        PaymentResult result2 = gateway.processPayment(new BigDecimal("200"), "INR", "Test2", "u2");
        PaymentResult result3 = gateway.processPayment(new BigDecimal("300"), "INR", "Test3", "u3");

        assertNotEquals(result1.getTransactionId(), result2.getTransactionId());
        assertNotEquals(result2.getTransactionId(), result3.getTransactionId());
        assertNotEquals(result1.getTransactionId(), result3.getTransactionId());
    }
}

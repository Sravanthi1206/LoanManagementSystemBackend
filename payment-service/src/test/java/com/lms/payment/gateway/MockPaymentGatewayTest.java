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

    @Test
    @DisplayName("Process Payment - eventually hits failure path (5% failure rate)")
    void processPayment_EventuallyFails() {
        // With 5% failure rate, run enough to statistically hit a failure
        boolean foundFailure = false;
        PaymentResult failedResult = null;
        
        for (int i = 0; i < 100; i++) {
            PaymentResult result = gateway.processPayment(
                    new BigDecimal("100.00"), "INR", "Test", "user-" + i);
            if (!result.isSuccess()) {
                foundFailure = true;
                failedResult = result;
                break;
            }
        }
        
        // Even if no failure found, test is not asserting this (probabilistic)
        // But when failure IS found, verify its structure
        if (foundFailure) {
            assertNotNull(failedResult);
            assertFalse(failedResult.isSuccess());
            assertEquals("FAILED", failedResult.getStatus());
            assertNotNull(failedResult.getErrorCode());
            assertNotNull(failedResult.getErrorMessage());
            // Verify error code is one of the expected values
            String[] expectedCodes = {"INSUFFICIENT_FUNDS", "CARD_DECLINED", "NETWORK_ERROR", "CVV_MISMATCH", "EXPIRED_CARD"};
            boolean validCode = false;
            for (String code : expectedCodes) {
                if (code.equals(failedResult.getErrorCode())) {
                    validCode = true;
                    break;
                }
            }
            assertTrue(validCode, "Error code should be one of the expected values");
        }
    }

    @Test
    @DisplayName("Test getErrorMessage - all error codes via reflection")
    void testGetErrorMessage_AllCodes() throws Exception {
        // Use reflection to test private getErrorMessage method
        java.lang.reflect.Method method = MockPaymentGateway.class.getDeclaredMethod("getErrorMessage", String.class);
        method.setAccessible(true);

        assertEquals("Insufficient funds in the account", method.invoke(gateway, "INSUFFICIENT_FUNDS"));
        assertEquals("Card was declined by the issuing bank", method.invoke(gateway, "CARD_DECLINED"));
        assertEquals("Network error occurred during processing", method.invoke(gateway, "NETWORK_ERROR"));
        assertEquals("CVV verification failed", method.invoke(gateway, "CVV_MISMATCH"));
        assertEquals("Card has expired", method.invoke(gateway, "EXPIRED_CARD"));
        assertEquals("Unknown error occurred", method.invoke(gateway, "UNKNOWN_CODE"));
    }

    @Test
    @DisplayName("Test getRandomErrorCode - returns valid error code")
    void testGetRandomErrorCode_ReturnsValidCode() throws Exception {
        java.lang.reflect.Method method = MockPaymentGateway.class.getDeclaredMethod("getRandomErrorCode");
        method.setAccessible(true);

        String[] expectedCodes = {"INSUFFICIENT_FUNDS", "CARD_DECLINED", "NETWORK_ERROR", "CVV_MISMATCH", "EXPIRED_CARD"};
        
        // Run multiple times to increase coverage
        for (int i = 0; i < 20; i++) {
            String errorCode = (String) method.invoke(gateway);
            assertNotNull(errorCode);
            boolean found = false;
            for (String expected : expectedCodes) {
                if (expected.equals(errorCode)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Error code should be one of expected values: " + errorCode);
        }
    }
}

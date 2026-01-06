package com.lms.payment.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentResult Tests")
class PaymentResultTest {

    private static final String TXN_ID = "TXN123";
    private static final String STATUS_SUCCESS = "SUCCESS";

    @Test
    @DisplayName("Should build PaymentResult with all fields")
    void shouldBuildPaymentResultWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        
        PaymentResult result = PaymentResult.builder()
                .success(true)
                .transactionId(TXN_ID)
                .gatewayTransactionId("GTW_TXN123")
                .status(STATUS_SUCCESS)
                .message("Payment processed")
                .amount(new BigDecimal("1000.00"))
                .currency("INR")
                .processedAt(now)
                .errorCode(null)
                .errorMessage(null)
                .build();

        assertTrue(result.isSuccess());
        assertEquals(TXN_ID, result.getTransactionId());
        assertEquals("GTW_TXN123", result.getGatewayTransactionId());
        assertEquals(STATUS_SUCCESS, result.getStatus());
        assertEquals("Payment processed", result.getMessage());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals("INR", result.getCurrency());
        assertEquals(now, result.getProcessedAt());
        assertNull(result.getErrorCode());
        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should build failed PaymentResult")
    void shouldBuildFailedPaymentResult() {
        PaymentResult result = PaymentResult.builder()
                .success(false)
                .transactionId("TXN456")
                .status("FAILED")
                .errorCode("INSUFFICIENT_FUNDS")
                .errorMessage("Not enough balance")
                .build();

        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getStatus());
        assertEquals("INSUFFICIENT_FUNDS", result.getErrorCode());
        assertEquals("Not enough balance", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        
        PaymentResult result1 = PaymentResult.builder()
                .success(true)
                .transactionId(TXN_ID)
                .gatewayTransactionId("GTW123")
                .status(STATUS_SUCCESS)
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .processedAt(now)
                .build();

        PaymentResult result2 = PaymentResult.builder()
                .success(true)
                .transactionId(TXN_ID)
                .gatewayTransactionId("GTW123")
                .status(STATUS_SUCCESS)
                .amount(new BigDecimal("500.00"))
                .currency("INR")
                .processedAt(now)
                .build();

        PaymentResult result3 = PaymentResult.builder()
                .success(false)
                .transactionId("TXN999")
                .build();

        // Test equals
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertNotNull(result1);
        assertNotEquals("string", result1);
        assertEquals(result1, result1);

        // Test hashCode
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        PaymentResult result = PaymentResult.builder()
                .success(true)
                .transactionId("TXN789")
                .status(STATUS_SUCCESS)
                .amount(new BigDecimal("250.00"))
                .build();

        String toString = result.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("TXN789"));
        assertTrue(toString.contains(STATUS_SUCCESS));
        assertTrue(toString.contains("250.00"));
    }

    @Test
    @DisplayName("Should test setters")
    void shouldTestSetters() {
        PaymentResult result = PaymentResult.builder().build();
        LocalDateTime now = LocalDateTime.now();

        result.setSuccess(true);
        result.setTransactionId("NEW_TXN");
        result.setGatewayTransactionId("NEW_GTW");
        result.setStatus("PENDING");
        result.setMessage("Processing");
        result.setAmount(new BigDecimal("100.00"));
        result.setCurrency("USD");
        result.setProcessedAt(now);
        result.setErrorCode("ERR001");
        result.setErrorMessage("Error occurred");

        assertTrue(result.isSuccess());
        assertEquals("NEW_TXN", result.getTransactionId());
        assertEquals("NEW_GTW", result.getGatewayTransactionId());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Processing", result.getMessage());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(now, result.getProcessedAt());
        assertEquals("ERR001", result.getErrorCode());
        assertEquals("Error occurred", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should test canEqual")
    void shouldTestCanEqual() {
        PaymentResult result1 = PaymentResult.builder()
                .success(true)
                .transactionId("TXN001")
                .build();

        PaymentResult result2 = PaymentResult.builder()
                .success(true)
                .transactionId("TXN001")
                .build();

        // canEqual is implicitly tested via equals
        assertEquals(result1, result2);
    }
}

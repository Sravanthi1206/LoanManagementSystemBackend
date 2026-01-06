package com.lms.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of PaymentGateway for testing and demo purposes.
 * Simulates payment processing with configurable success rate.
 */
@Component
@Slf4j
public class MockPaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "MockPaymentGateway";
    private static final double SUCCESS_RATE = 0.95; // 95% success rate
    
    private final Random random = new Random();
    private final Map<String, PaymentResult> transactionStore = new ConcurrentHashMap<>();

    @Override
    public PaymentResult processPayment(BigDecimal amount, String currency, String description, String customerId) {
        log.info("[MockGateway] Processing payment: amount={}, currency={}, customer={}", amount, currency, customerId);
        
        // Simulate processing delay
        simulateNetworkDelay();
        
        String gatewayTxnId = generateGatewayTransactionId();
        boolean isSuccess = random.nextDouble() < SUCCESS_RATE;
        
        PaymentResult result;
        if (isSuccess) {
            result = PaymentResult.builder()
                    .success(true)
                    .transactionId(gatewayTxnId)
                    .gatewayTransactionId("MOCK_" + gatewayTxnId)
                    .status("SUCCESS")
                    .message("Payment processed successfully")
                    .amount(amount)
                    .currency(currency)
                    .processedAt(LocalDateTime.now())
                    .build();
            log.info("[MockGateway] Payment successful: txnId={}", gatewayTxnId);
        } else {
            String errorCode = getRandomErrorCode();
            result = PaymentResult.builder()
                    .success(false)
                    .transactionId(gatewayTxnId)
                    .gatewayTransactionId("MOCK_" + gatewayTxnId)
                    .status("FAILED")
                    .message("Payment failed")
                    .amount(amount)
                    .currency(currency)
                    .processedAt(LocalDateTime.now())
                    .errorCode(errorCode)
                    .errorMessage(getErrorMessage(errorCode))
                    .build();
            log.warn("[MockGateway] Payment failed: txnId={}, error={}", gatewayTxnId, errorCode);
        }
        
        transactionStore.put(gatewayTxnId, result);
        return result;
    }

    @Override
    public PaymentResult initiateRefund(String transactionId, BigDecimal amount) {
        log.info("[MockGateway] Processing refund: originalTxn={}, amount={}", transactionId, amount);
        
        simulateNetworkDelay();
        
        PaymentResult originalTxn = transactionStore.get(transactionId);
        if (originalTxn == null) {
            return PaymentResult.builder()
                    .success(false)
                    .transactionId(transactionId)
                    .status("FAILED")
                    .errorCode("TXN_NOT_FOUND")
                    .errorMessage("Original transaction not found")
                    .processedAt(LocalDateTime.now())
                    .build();
        }
        
        String refundTxnId = "REFUND_" + generateGatewayTransactionId();
        PaymentResult result = PaymentResult.builder()
                .success(true)
                .transactionId(refundTxnId)
                .gatewayTransactionId("MOCK_" + refundTxnId)
                .status("REFUNDED")
                .message("Refund processed successfully")
                .amount(amount)
                .currency(originalTxn.getCurrency())
                .processedAt(LocalDateTime.now())
                .build();
        
        transactionStore.put(refundTxnId, result);
        log.info("[MockGateway] Refund successful: refundTxnId={}", refundTxnId);
        return result;
    }

    @Override
    public PaymentResult getTransactionStatus(String transactionId) {
        log.info("[MockGateway] Checking status: txnId={}", transactionId);
        
        PaymentResult result = transactionStore.get(transactionId);
        if (result == null) {
            return PaymentResult.builder()
                    .success(false)
                    .transactionId(transactionId)
                    .status("NOT_FOUND")
                    .errorCode("TXN_NOT_FOUND")
                    .errorMessage("Transaction not found")
                    .processedAt(LocalDateTime.now())
                    .build();
        }
        return result;
    }

    @Override
    public String getGatewayName() {
        return GATEWAY_NAME;
    }

    private String generateGatewayTransactionId() {
        return "MPAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(100 + random.nextInt(200)); // 100-300ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String getRandomErrorCode() {
        String[] errorCodes = {"INSUFFICIENT_FUNDS", "CARD_DECLINED", "NETWORK_ERROR", "CVV_MISMATCH", "EXPIRED_CARD"};
        return errorCodes[random.nextInt(errorCodes.length)];
    }

    private String getErrorMessage(String errorCode) {
        return switch (errorCode) {
            case "INSUFFICIENT_FUNDS" -> "Insufficient funds in the account";
            case "CARD_DECLINED" -> "Card was declined by the issuing bank";
            case "NETWORK_ERROR" -> "Network error occurred during processing";
            case "CVV_MISMATCH" -> "CVV verification failed";
            case "EXPIRED_CARD" -> "Card has expired";
            default -> "Unknown error occurred";
        };
    }
}

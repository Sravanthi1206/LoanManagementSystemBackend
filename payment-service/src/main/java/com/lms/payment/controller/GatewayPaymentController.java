package com.lms.payment.controller;

import com.lms.payment.dto.GatewayPaymentRequest;
import com.lms.payment.dto.WalletTopupRequest;
import com.lms.payment.entity.Payment;
import com.lms.payment.gateway.MockPaymentGateway;
import com.lms.payment.gateway.PaymentResult;
import com.lms.payment.repository.PaymentRepository;
import com.lms.payment.client.LoanClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller for payment gateway operations.
 * Uses mock gateway for demo/testing purposes.
 */
@RestController
@RequestMapping("/payments/gateway")
@RequiredArgsConstructor
@Slf4j
public class GatewayPaymentController {

    private final MockPaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final LoanClient loanClient;

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_TRANSACTION_ID = "transactionId";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_ERROR_CODE = "errorCode";
    private static final String KEY_ERROR_MESSAGE = "errorMessage";

    @PostMapping("/pay")
    public ResponseEntity<?> processPayment(@RequestBody GatewayPaymentRequest request) {
        log.info("Processing gateway payment for loan {} amount {}", request.getLoanId(), request.getAmount());
        
        // Process through mock gateway
        PaymentResult result = paymentGateway.processPayment(
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                request.getUserId().toString()
        );
        
        if (result.isSuccess()) {
            // Record successful payment 
            Payment payment = Payment.builder()
                    .loanId(request.getLoanId())
                    .userId(request.getUserId())
                    .paymentType(Payment.PaymentType.EMI_REPAYMENT)
                    .amount(request.getAmount())
                    .paymentMethod(Payment.PaymentMethod.DEBIT_CARD)
                    .transactionId(result.getTransactionId())
                    .referenceNumber(result.getGatewayTransactionId())
                    .status(Payment.PaymentStatus.SUCCESS)
                    .paymentDate(LocalDateTime.now())
                    .build();
            
            paymentRepository.save(payment);
            
            return ResponseEntity.ok(Map.of(
                    KEY_SUCCESS, true,
                    KEY_TRANSACTION_ID, result.getTransactionId(),
                    "gatewayTransactionId", result.getGatewayTransactionId(),
                    KEY_MESSAGE, result.getMessage(),
                    KEY_AMOUNT, result.getAmount()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    KEY_SUCCESS, false,
                    KEY_ERROR_CODE, result.getErrorCode(),
                    KEY_ERROR_MESSAGE, result.getErrorMessage()
            ));
        }
    }

    @PostMapping("/wallet/topup")
    public ResponseEntity<?> topupWallet(@RequestBody WalletTopupRequest request) {
        log.info("Processing wallet top-up for user {} amount {}", request.getUserId(), request.getAmount());
        
        // Process payment through gateway
        PaymentResult result = paymentGateway.processPayment(
                request.getAmount(),
                request.getCurrency(),
                "Wallet Top-up",
                request.getUserId().toString()
        );
        
        if (result.isSuccess()) {
            // Credit wallet in loan-service
            try {
                loanClient.creditWallet(request.getUserId(), request.getAmount(), 
                    "Top-up via " + request.getPaymentMethod() + " - " + result.getGatewayTransactionId());
                
                log.info("Wallet credited successfully for user {}", request.getUserId());
                
                return ResponseEntity.ok(Map.of(
                        KEY_SUCCESS, true,
                        KEY_TRANSACTION_ID, result.getTransactionId(),
                        "gatewayTransactionId", result.getGatewayTransactionId(),
                        KEY_MESSAGE, "Wallet topped up successfully",
                        KEY_AMOUNT, result.getAmount()
                ));
            } catch (Exception e) {
                log.error("Failed to credit wallet after successful payment: {}", e.getMessage());
                // Payment succeeded but wallet credit failed - needs manual intervention
                return ResponseEntity.status(500).body(Map.of(
                        KEY_SUCCESS, false,
                        KEY_TRANSACTION_ID, result.getTransactionId(),
                        KEY_ERROR_CODE, "WALLET_CREDIT_FAILED",
                        KEY_ERROR_MESSAGE, "Payment succeeded but wallet credit failed. Contact support with transaction ID."
                ));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    KEY_SUCCESS, false,
                    KEY_ERROR_CODE, result.getErrorCode(),
                    KEY_ERROR_MESSAGE, result.getErrorMessage()
            ));
        }
    }

    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<?> processRefund(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> body) {
        
        var amount = new java.math.BigDecimal(body.get(KEY_AMOUNT).toString());
        log.info("Processing refund for transaction {} amount {}", transactionId, amount);
        
        PaymentResult result = paymentGateway.initiateRefund(transactionId, amount);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                    KEY_SUCCESS, true,
                    "refundTransactionId", result.getTransactionId(),
                    KEY_MESSAGE, result.getMessage(),
                    KEY_AMOUNT, result.getAmount()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    KEY_SUCCESS, false,
                    KEY_ERROR_CODE, result.getErrorCode(),
                    KEY_ERROR_MESSAGE, result.getErrorMessage()
            ));
        }
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<PaymentResult> getTransactionStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentGateway.getTransactionStatus(transactionId));
    }

    @GetMapping("/info")
    public ResponseEntity<?> getGatewayInfo() {
        return ResponseEntity.ok(Map.of(
                "gateway", paymentGateway.getGatewayName(),
                "supportedMethods", new String[]{"CARD", "UPI", "NET_BANKING"},
                "currencies", new String[]{"INR"},
                "mode", "MOCK"
        ));
    }
}


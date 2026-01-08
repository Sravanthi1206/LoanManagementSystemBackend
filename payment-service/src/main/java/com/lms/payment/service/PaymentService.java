package com.lms.payment.service;

import com.lms.payment.dto.DisbursementRequest;
import com.lms.payment.dto.PaymentResponse;
import com.lms.payment.dto.RepaymentRequest;
import com.lms.payment.entity.Payment;
import com.lms.payment.repository.PaymentRepository;
import com.lms.payment.client.IdentityClient;
import com.lms.payment.messaging.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository repository;
    private final com.lms.payment.client.LoanClient loanClient;
    private final com.lms.payment.client.EmiClient emiClient;
    private final IdentityClient identityClient;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public PaymentResponse recordDisbursement(DisbursementRequest request) {
        Payment payment = Payment.builder()
                .loanId(request.getLoanId())
                .userId(request.getUserId())
                .paymentType(Payment.PaymentType.DISBURSEMENT)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .transactionId(generateTransactionId())
                .status(Payment.PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        Payment saved = repository.save(payment);
        return mapToResponse(saved);
    }

    @Transactional
    public PaymentResponse recordRepayment(RepaymentRequest request) {
        log.info("Processing repayment for user {} on loan {}, amount: {}", 
                 request.getUserId(), request.getLoanId(), request.getAmount());
        
        // 1. If Wallet payment, deduct balance
        if (request.getPaymentMethod() == Payment.PaymentMethod.WALLET) {
            try {
                log.info("Debiting wallet for user {}", request.getUserId());
                loanClient.debitWallet(request.getUserId(), request.getAmount());
            } catch (Exception e) {
                log.error("Failed to debit wallet for user {}: {}", request.getUserId(), e.getMessage());
                throw new RuntimeException("Failed to process wallet payment: " + e.getMessage(), e);
            }
        }

        // 2. Mark EMI as paid if installment ID is provided
        if (request.getInstallmentId() != null) {
            try {
                log.info("Marking installment {} as paid", request.getInstallmentId());
                emiClient.markInstallmentAsPaid(request.getInstallmentId());
            } catch (Exception e) {
                log.error("Failed to mark installment {} as paid: {}", request.getInstallmentId(), e.getMessage());
                log.warn("Payment will be recorded but EMI status may need manual update");
            }
        }

        Payment payment = Payment.builder()
                .loanId(request.getLoanId())
                .userId(request.getUserId())
                .paymentType(Payment.PaymentType.EMI_REPAYMENT)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .transactionId(generateTransactionId())
                .status(Payment.PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        Payment saved = repository.save(payment);
        log.info("Payment recorded successfully with transaction ID: {}", saved.getTransactionId());
        
        // 3. Increment user's credit score for successful payment
        try {
            Map<String, Integer> scoreResult = identityClient.incrementCreditScore(request.getUserId());
            int newScore = scoreResult.getOrDefault("creditScore", -1);
            if (newScore > 0) {
                log.info("User {} credit score incremented to {}", request.getUserId(), newScore);
            }
        } catch (Exception e) {
            log.warn("Failed to increment credit score for user {}: {}", request.getUserId(), e.getMessage());
        }
        
        // 4. Send payment confirmation notification
        try {
            notificationPublisher.sendPaymentNotification(
                request.getUserId(),
                request.getLoanId(),
                "PAYMENT_SUCCESS",
                "Payment Received",
                String.format("Your EMI payment of ₹%.2f for loan #%d has been received. Transaction ID: %s",
                    request.getAmount(), request.getLoanId(), saved.getTransactionId()),
                request.getUserEmail()
            );
        } catch (Exception e) {
            log.warn("Failed to send payment notification: {}", e.getMessage());
        }
        
        return mapToResponse(saved);
    }

    public List<PaymentResponse> getPaymentsByLoan(Long loanId) {
        return repository.findByLoanId(loanId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<PaymentResponse> getPaymentsByUser(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new com.lms.payment.exception.PaymentNotFoundException(id));
        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new com.lms.payment.exception.PaymentNotFoundException(transactionId));
        return mapToResponse(payment);
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .loanId(payment.getLoanId())
                .userId(payment.getUserId())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .referenceNumber(payment.getReferenceNumber())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

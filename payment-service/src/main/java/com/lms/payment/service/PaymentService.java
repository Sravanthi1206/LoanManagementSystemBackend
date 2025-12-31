package com.lms.payment.service;

import com.lms.payment.dto.DisbursementRequest;
import com.lms.payment.dto.PaymentResponse;
import com.lms.payment.dto.RepaymentRequest;
import com.lms.payment.entity.Payment;
import com.lms.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;

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
        
        // TODO: Call EMI Service to mark installment as paid
        
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
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction id: " + transactionId));
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

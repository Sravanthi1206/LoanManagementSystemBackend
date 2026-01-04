package com.lms.payment.controller;

import com.lms.payment.dto.CreatePaymentResponse;
import com.lms.payment.dto.DisbursementRequest;
import com.lms.payment.dto.PaymentResponse;
import com.lms.payment.dto.RepaymentRequest;
import com.lms.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/disburse")
    public ResponseEntity<CreatePaymentResponse> recordDisbursement(
            @Valid @RequestBody DisbursementRequest request) {
        PaymentResponse response = paymentService.recordDisbursement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreatePaymentResponse.builder().id(response.getId()).transactionId(response.getTransactionId()).build());
    }

    @PostMapping("/repay")
    public ResponseEntity<CreatePaymentResponse> recordRepayment(
            @Valid @RequestBody RepaymentRequest request) {
        PaymentResponse response = paymentService.recordRepayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreatePaymentResponse.builder().id(response.getId()).transactionId(response.getTransactionId()).build());
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(paymentService.getPaymentsByLoan(loanId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "paymentDate") Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentByTransactionId(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPaymentByTransactionId(transactionId));
    }
}

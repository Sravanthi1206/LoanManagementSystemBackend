package com.lms.payment.service;

import com.lms.payment.dto.DisbursementRequest;
import com.lms.payment.dto.PaymentResponse;
import com.lms.payment.dto.RepaymentRequest;
import com.lms.payment.entity.Payment;
import com.lms.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository repository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private DisbursementRequest disbursementRequest;
    private RepaymentRequest repaymentRequest;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id(1L)
                .loanId(101L)
                .userId(1L)
                .paymentType(Payment.PaymentType.DISBURSEMENT)
                .amount(new BigDecimal("50000.00"))
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId("TXN123")
                .paymentDate(LocalDateTime.now())
                .build();

        disbursementRequest = DisbursementRequest.builder()
                .loanId(101L)
                .userId(1L)
                .amount(new BigDecimal("50000.00"))
                .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                .build();

        repaymentRequest = RepaymentRequest.builder()
                .loanId(101L)
                .userId(1L)
                .amount(new BigDecimal("5000.00"))
                .paymentMethod(Payment.PaymentMethod.ONLINE)
                .build();
    }

    @Test
    @DisplayName("Record Disbursement - Success")
    void recordDisbursement_Success() {
        when(repository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.recordDisbursement(disbursementRequest);

        assertNotNull(response);
        assertEquals(Payment.PaymentStatus.SUCCESS, response.getStatus());
        verify(repository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Record Repayment - Success")
    void recordRepayment_Success() {
        payment.setPaymentType(Payment.PaymentType.EMI_REPAYMENT);
        payment.setAmount(new BigDecimal("5000.00"));
        
        when(repository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.recordRepayment(repaymentRequest);

        assertNotNull(response);
        assertEquals(Payment.PaymentType.EMI_REPAYMENT, response.getPaymentType());
        verify(repository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Get Payments by Loan - Success")
    void getPaymentsByLoan_Success() {
        when(repository.findByLoanId(101L)).thenReturn(Collections.singletonList(payment));

        List<PaymentResponse> results = paymentService.getPaymentsByLoan(101L);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Get Payments by User - Success")
    void getPaymentsByUser_Success() {
        Page<Payment> page = new PageImpl<>(Collections.singletonList(payment));
        when(repository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(page);

        Page<PaymentResponse> result = paymentService.getPaymentsByUser(1L, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Get Payment by ID - Success")
    void getPaymentById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }
    @Test
    @DisplayName("Get Payment by ID - Not Found")
    void getPaymentById_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentById(999L));
    }

    @Test
    @DisplayName("Get Payment by Transaction ID - Success")
    void getPaymentByTransactionId_Success() {
        when(repository.findByTransactionId("TXN123")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByTransactionId("TXN123");

        assertNotNull(response);
        assertEquals("TXN123", response.getTransactionId());
    }

    @Test
    @DisplayName("Get Payment by Transaction ID - Not Found")
    void getPaymentByTransactionId_NotFound() {
        when(repository.findByTransactionId("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentByTransactionId("INVALID"));
    }
}

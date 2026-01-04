package com.lms.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.payment.dto.DisbursementRequest;
import com.lms.payment.dto.PaymentResponse;
import com.lms.payment.dto.RepaymentRequest;
import com.lms.payment.entity.Payment;
import com.lms.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Payment Controller Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private DisbursementRequest disbursementRequest;
    private RepaymentRequest repaymentRequest;
    private PaymentResponse response;

    @BeforeEach
    void setUp() {
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

        response = PaymentResponse.builder()
                .id(1L)
                .loanId(101L)
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId("TXN123")
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /payments/disburse - Success")
    void recordDisbursement_Success() throws Exception {
        when(paymentService.recordDisbursement(any(DisbursementRequest.class))).thenReturn(response);

        mockMvc.perform(post("/payments/disburse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(disbursementRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN123"));
    }

    @Test
    @DisplayName("POST /payments/repay - Success")
    void recordRepayment_Success() throws Exception {
        when(paymentService.recordRepayment(any(RepaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/payments/repay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repaymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN123"));
    }

    @Test
    @DisplayName("GET /payments/loan/{loanId} - Success")
    void getPaymentsByLoan_Success() throws Exception {
        List<PaymentResponse> list = Collections.singletonList(response);
        when(paymentService.getPaymentsByLoan(101L)).thenReturn(list);

        mockMvc.perform(get("/payments/loan/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("TXN123"));
    }

    @Test
    @DisplayName("GET /payments/user/{userId} - Success")
    void getPaymentsByUser_Success() throws Exception {
        Page<PaymentResponse> page = new PageImpl<>(Collections.singletonList(response));
        when(paymentService.getPaymentsByUser(anyLong(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value("TXN123"));
    }
    @Test
    @DisplayName("GET /payments/{id} - Success")
    void getPaymentById_Success() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(response);

        mockMvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN123"));
    }

    @Test
    @DisplayName("GET /payments/transaction/{transactionId} - Success")
    void getPaymentByTransactionId_Success() throws Exception {
        when(paymentService.getPaymentByTransactionId("TXN123")).thenReturn(response);

        mockMvc.perform(get("/payments/transaction/TXN123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN123"));
    }
}

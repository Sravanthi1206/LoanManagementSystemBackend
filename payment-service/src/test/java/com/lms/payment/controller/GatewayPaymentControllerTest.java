package com.lms.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.payment.client.LoanClient;
import com.lms.payment.dto.GatewayPaymentRequest;
import com.lms.payment.dto.WalletTopupRequest;
import com.lms.payment.gateway.MockPaymentGateway;
import com.lms.payment.gateway.PaymentResult;
import com.lms.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GatewayPaymentController.class)
@DisplayName("Gateway Payment Controller Tests")
class GatewayPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MockPaymentGateway paymentGateway;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private LoanClient loanClient;

    @Test
    @DisplayName("Process Payment - success")
    void processPayment_Success() throws Exception {
        GatewayPaymentRequest request = new GatewayPaymentRequest();
        request.setUserId(1L);
        request.setLoanId(100L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setCurrency("INR");
        request.setDescription("EMI Payment");

        PaymentResult successResult = PaymentResult.builder()
                .success(true)
                .transactionId("TXN-12345")
                .gatewayTransactionId("MOCK_TXN-12345")
                .status("SUCCESS")
                .message("Payment processed successfully")
                .amount(new BigDecimal("5000.00"))
                .currency("INR")
                .processedAt(LocalDateTime.now())
                .build();

        when(paymentGateway.processPayment(any(BigDecimal.class), anyString(), anyString(), anyString()))
                .thenReturn(successResult);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(post("/payments/gateway/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value("TXN-12345"))
                .andExpect(jsonPath("$.amount").value(5000.00));

        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("Process Payment - failure")
    void processPayment_Failure() throws Exception {
        GatewayPaymentRequest request = new GatewayPaymentRequest();
        request.setUserId(1L);
        request.setLoanId(100L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setCurrency("INR");
        request.setDescription("EMI Payment");

        PaymentResult failureResult = PaymentResult.builder()
                .success(false)
                .transactionId("TXN-12345")
                .status("FAILED")
                .errorCode("INSUFFICIENT_FUNDS")
                .errorMessage("Card has insufficient funds")
                .build();

        when(paymentGateway.processPayment(any(BigDecimal.class), anyString(), anyString(), anyString()))
                .thenReturn(failureResult);

        mockMvc.perform(post("/payments/gateway/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Wallet Topup - success")
    void walletTopup_Success() throws Exception {
        WalletTopupRequest request = new WalletTopupRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("10000.00"));
        request.setPaymentMethod("CARD");

        PaymentResult successResult = PaymentResult.builder()
                .success(true)
                .transactionId("TXN-TOP-123")
                .gatewayTransactionId("MOCK_TXN-TOP-123")
                .status("SUCCESS")
                .message("Payment processed successfully")
                .amount(new BigDecimal("10000.00"))
                .processedAt(LocalDateTime.now())
                .build();

        when(paymentGateway.processPayment(any(BigDecimal.class), anyString(), anyString(), anyString()))
                .thenReturn(successResult);
        doNothing().when(loanClient).creditWallet(anyLong(), any(BigDecimal.class), anyString());

        mockMvc.perform(post("/payments/gateway/wallet/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value("TXN-TOP-123"))
                .andExpect(jsonPath("$.message").value("Wallet topped up successfully"));

        verify(loanClient).creditWallet(eq(1L), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("Wallet Topup - gateway failure")
    void walletTopup_GatewayFailure() throws Exception {
        WalletTopupRequest request = new WalletTopupRequest();
        request.setUserId(1L);
        request.setAmount(new BigDecimal("10000.00"));
        request.setPaymentMethod("CARD");
        request.setCurrency("INR");

        PaymentResult failureResult = PaymentResult.builder()
                .success(false)
                .transactionId("TXN-TOP-123")
                .errorCode("CARD_DECLINED")
                .errorMessage("Card was declined")
                .build();

        when(paymentGateway.processPayment(any(BigDecimal.class), anyString(), anyString(), anyString()))
                .thenReturn(failureResult);

        mockMvc.perform(post("/payments/gateway/wallet/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CARD_DECLINED"));

        verify(loanClient, never()).creditWallet(anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("Refund - success")
    void refund_Success() throws Exception {
        PaymentResult refundResult = PaymentResult.builder()
                .success(true)
                .transactionId("REF-12345")
                .status("REFUNDED")
                .message("Refund processed successfully")
                .amount(new BigDecimal("1000.00"))
                .build();

        when(paymentGateway.initiateRefund(anyString(), any(BigDecimal.class))).thenReturn(refundResult);

        mockMvc.perform(post("/payments/gateway/refund/TXN-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.refundTransactionId").value("REF-12345"));
    }

    @Test
    @DisplayName("Refund - failure")
    void refund_Failure() throws Exception {
        PaymentResult failResult = PaymentResult.builder()
                .success(false)
                .transactionId("REF-12345")
                .errorCode("TXN_NOT_FOUND")
                .errorMessage("Original transaction not found")
                .build();

        when(paymentGateway.initiateRefund(anyString(), any(BigDecimal.class))).thenReturn(failResult);

        mockMvc.perform(post("/payments/gateway/refund/INVALID-TXN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("TXN_NOT_FOUND"));
    }

    @Test
    @DisplayName("Get Transaction Status")
    void getTransactionStatus() throws Exception {
        PaymentResult result = PaymentResult.builder()
                .success(true)
                .transactionId("TXN-12345")
                .status("SUCCESS")
                .amount(new BigDecimal("5000.00"))
                .build();

        when(paymentGateway.getTransactionStatus("TXN-12345")).thenReturn(result);

        mockMvc.perform(get("/payments/gateway/status/TXN-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-12345"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("Get Gateway Info")
    void getGatewayInfo() throws Exception {
        when(paymentGateway.getGatewayName()).thenReturn("MockPaymentGateway");

        mockMvc.perform(get("/payments/gateway/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateway").value("MockPaymentGateway"))
                .andExpect(jsonPath("$.mode").value("MOCK"));
    }
}

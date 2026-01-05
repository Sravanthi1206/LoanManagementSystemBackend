package com.lms.loan.controller;

import com.lms.loan.dto.TransactionResponse;
import com.lms.loan.dto.WalletResponse;
import com.lms.loan.service.WalletService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Wallet Controller Tests")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    @DisplayName("Get Balance - success")
    void getBalance() throws Exception {
        WalletResponse response = WalletResponse.builder().userId(1L).balance(new BigDecimal("1000.00")).build();
        when(walletService.getBalance(1L)).thenReturn(response);

        mockMvc.perform(get("/wallet/balance")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000.0));
    }
    
    @Test
    @DisplayName("Get Balance - missing user id")
    void getBalance_MissingId() throws Exception {
        // Expect 4xx or 5xx depending on exception handling
        mockMvc.perform(get("/wallet/balance"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Get Transactions")
    void getTransactions() throws Exception {
        TransactionResponse tx = TransactionResponse.builder().transactionId("TX1").build();
        Page<TransactionResponse> page = new PageImpl<>(Collections.singletonList(tx));

        when(walletService.getTransactionHistory(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/wallet/transactions")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value("TX1"));
    }

    @Test
    @DisplayName("Get Balance By Path Variable")
    void getBalanceByPathVariable() throws Exception {
         WalletResponse response = WalletResponse.builder().userId(1L).balance(new BigDecimal("2000.00")).build();
        when(walletService.getBalance(1L)).thenReturn(response);

        mockMvc.perform(get("/wallet/balance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(2000.0));
    }
}

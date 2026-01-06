package com.lms.loan.controller;

import com.lms.loan.entity.Loan;
import com.lms.loan.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    private static final String TOTAL_LOANS_PATH = "$.totalLoans";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanRepository loanRepository;

    private Loan testLoan;

    @BeforeEach
    void setUp() {
        testLoan = Loan.builder()
                .loanId(1L)
                .userId(1L)
                .type(Loan.LoanType.PERSONAL)
                .amountRequested(new BigDecimal("100000"))
                .status(Loan.LoanStatus.APPLIED)
                .build();
    }

    @Test
    @DisplayName("GET /dashboard/stats - Success")
    void getDashboardStatsShouldSucceed() throws Exception {
        when(loanRepository.findAll()).thenReturn(Arrays.asList(testLoan));

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TOTAL_LOANS_PATH).value(1));
    }

    @Test
    @DisplayName("GET /dashboard/stats - Empty")
    void getDashboardStatsShouldReturnEmptyWhenNoLoans() throws Exception {
        when(loanRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TOTAL_LOANS_PATH).value(0));
    }

    @Test
    @DisplayName("GET /dashboard/loans-by-status - Success")
    void getLoansByStatusShouldSucceed() throws Exception {
        when(loanRepository.findAll()).thenReturn(Arrays.asList(testLoan));

        mockMvc.perform(get("/dashboard/loans-by-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /dashboard/loans-by-type - Success")
    void getLoansByTypeShouldSucceed() throws Exception {
        when(loanRepository.findAll()).thenReturn(Arrays.asList(testLoan));

        mockMvc.perform(get("/dashboard/loans-by-type"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /dashboard/customer-summary/{userId} - Success")
    void getCustomerSummaryShouldSucceed() throws Exception {
        when(loanRepository.findByUserId(1L)).thenReturn(Arrays.asList(testLoan));

        mockMvc.perform(get("/dashboard/customer-summary/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TOTAL_LOANS_PATH).value(1));
    }
}

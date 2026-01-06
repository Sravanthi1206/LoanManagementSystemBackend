package com.lms.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.loan.dto.*;
import com.lms.loan.entity.Loan;
import com.lms.loan.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LoanApplicationController Tests")
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    private LoanApplicationRequest applicationRequest;
    private LoanApplicationResponse applicationResponse;

    @BeforeEach
    void setUp() {
        applicationRequest = new LoanApplicationRequest();
        applicationRequest.setUserId(1L);
        applicationRequest.setType(Loan.LoanType.PERSONAL);
        applicationRequest.setAmount(new BigDecimal("100000"));
        applicationRequest.setPurpose("Home Renovation");
        applicationRequest.setTenure(24);
        applicationRequest.setEmploymentType(Loan.EmploymentType.SALARIED);
        applicationRequest.setMonthlyIncome(new BigDecimal("50000"));
        applicationRequest.setAnnualIncome(new BigDecimal("600000"));

        applicationResponse = LoanApplicationResponse.builder()
                .loanId(1L)
                .userId(1L)
                .type(Loan.LoanType.PERSONAL)
                .amountRequested(new BigDecimal("100000"))
                .purpose("Home Renovation")
                .tenureMonths(24)
                .status(Loan.LoanStatus.APPLIED)
                .build();
    }

    @Nested
    @DisplayName("Apply for Loan Tests")
    class ApplyForLoanTests {

        @Test
        @DisplayName("POST /loans/apply - Success")
        void applyForLoanShouldSucceed() throws Exception {
            when(loanService.applyLoan(any(LoanApplicationRequest.class)))
                .thenReturn(applicationResponse);

            mockMvc.perform(post("/loans/apply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(applicationRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.loanId").value(1));
        }
    }

    @Nested
    @DisplayName("Get Loans Tests")
    class GetLoansTests {

        @Test
        @DisplayName("GET /loans/my-loans - Success")
        void getMyLoansShouldSucceed() throws Exception {
            List<LoanApplicationResponse> loans = Arrays.asList(applicationResponse);
            when(loanService.getMyLoans(anyLong())).thenReturn(loans);

            mockMvc.perform(get("/loans/my-loans")
                    .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].loanId").value(1));
        }

        @Test
        @DisplayName("GET /loans/{id} - Success")
        void getLoanByIdShouldSucceed() throws Exception {
            when(loanService.getLoanById(1L)).thenReturn(applicationResponse);

            mockMvc.perform(get("/loans/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.loanId").value(1));
        }
    }

    @Nested
    @DisplayName("Withdraw Loan Tests")
    class WithdrawLoanTests {

        @Test
        @DisplayName("PUT /loans/{id}/withdraw - Success")
        void withdrawLoanShouldSucceed() throws Exception {
            applicationResponse.setStatus(Loan.LoanStatus.WITHDRAWN);
            when(loanService.withdrawLoan(eq(1L), anyLong())).thenReturn(applicationResponse);

            mockMvc.perform(put("/loans/1/withdraw")
                    .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        }
    }
}

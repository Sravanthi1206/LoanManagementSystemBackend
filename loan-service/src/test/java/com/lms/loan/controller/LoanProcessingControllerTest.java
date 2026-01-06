package com.lms.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.loan.dto.CreditCheckRequest;
import com.lms.loan.dto.LoanApplicationResponse;
import com.lms.loan.dto.LoanApprovalRequest;
import com.lms.loan.dto.RejectRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanProcessingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LoanProcessingController Tests")
class LoanProcessingControllerTest {

    private static final String STATUS_PATH = "$.status";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    private LoanApplicationResponse loanResponse;

    @BeforeEach
    void setUp() {
        loanResponse = LoanApplicationResponse.builder()
                .loanId(1L)
                .userId(1L)
                .type(Loan.LoanType.PERSONAL)
                .amountRequested(new BigDecimal("100000"))
                .status(Loan.LoanStatus.APPLIED)
                .build();
    }

    @Nested
    @DisplayName("Get Loans Tests")
    class GetLoansTests {

        @Test
        @DisplayName("GET /loans/admin - Success")
        void getAllLoansShouldSucceed() throws Exception {
            Page<LoanApplicationResponse> page = new PageImpl<>(Collections.singletonList(loanResponse));
            when(loanService.getAllLoans(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/loans/admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].loanId").value(1));
        }

        @Test
        @DisplayName("GET /loans/admin/pending - Success")
        void getPendingLoansShouldSucceed() throws Exception {
            Page<LoanApplicationResponse> page = new PageImpl<>(Collections.singletonList(loanResponse));
            when(loanService.getLoansByStatus(eq(Loan.LoanStatus.APPLIED), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/loans/admin/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].loanId").value(1));
        }

        @Test
        @DisplayName("GET /loans/admin/under-review - Success")
        void getUnderReviewLoansShouldSucceed() throws Exception {
            loanResponse.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            Page<LoanApplicationResponse> page = new PageImpl<>(Collections.singletonList(loanResponse));
            when(loanService.getLoansByStatus(eq(Loan.LoanStatus.UNDER_REVIEW), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/loans/admin/under-review"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status").value("UNDER_REVIEW"));
        }

        @Test
        @DisplayName("GET /loans/admin - As Officer - Should Return Assigned Loans")
        void getAllLoansAsOfficer_ShouldReturnAssignedLoans() throws Exception {
            Page<LoanApplicationResponse> page = new PageImpl<>(Collections.singletonList(loanResponse));
            when(loanService.getMyAssignedLoans(eq(100L), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/loans/admin")
                    .header("X-User-Role", "LOAN_OFFICER")
                    .header("X-User-Id", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].loanId").value(1));
        }

        @Test
        @DisplayName("GET /loans/admin/by-status/{status} - As Officer - Should Return Assigned Loans")
        void getLoansByStatusAsOfficer_ShouldReturnAssignedLoans() throws Exception {
            Page<LoanApplicationResponse> page = new PageImpl<>(Collections.singletonList(loanResponse));
            when(loanService.getMyLoansByStatus(eq(100L), eq(Loan.LoanStatus.APPLIED), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/loans/admin/by-status/APPLIED")
                    .header("X-User-Role", "LOAN_OFFICER")
                    .header("X-User-Id", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].loanId").value(1));
        }
    }

    @Nested
    @DisplayName("Process Loan Tests")
    class ProcessLoanTests {

        @Test
        @DisplayName("PUT /loans/admin/{id}/review - Success")
        void startReviewShouldSucceed() throws Exception {
            loanResponse.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            when(loanService.reviewLoan(eq(1L), anyLong())).thenReturn(loanResponse);

            mockMvc.perform(put("/loans/admin/1/review")
                    .header("X-User-Id", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(STATUS_PATH).value("UNDER_REVIEW"));
        }

        @Test
        @DisplayName("POST /loans/admin/{id}/credit-check - Success")
        void performCreditCheckShouldSucceed() throws Exception {
            CreditCheckRequest request = new CreditCheckRequest();
            request.setCreditScore(750);
            request.setRemarks("Credit check passed successfully");

            when(loanService.performCreditCheck(eq(1L), eq(750), eq("Credit check passed successfully")))
                    .thenReturn(loanResponse);

            mockMvc.perform(post("/loans/admin/1/credit-check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /loans/admin/{id}/approve - Success")
        void approveLoanShouldSucceed() throws Exception {
            LoanApprovalRequest request = new LoanApprovalRequest();
            request.setApprovedAmount(new BigDecimal("90000"));
            request.setInterestRate(new BigDecimal("12"));
            request.setRemarks("Approved");

            loanResponse.setStatus(Loan.LoanStatus.APPROVED);
            when(loanService.approveLoan(eq(1L), any(LoanApprovalRequest.class)))
                    .thenReturn(loanResponse);

            mockMvc.perform(put("/loans/admin/1/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(STATUS_PATH).value("APPROVED"));
        }

        @Test
        @DisplayName("PUT /loans/admin/{id}/reject - Success")
        void rejectLoanShouldSucceed() throws Exception {
            RejectRequest request = new RejectRequest();
            request.setRemarks("Rejected due to policy violation");

            loanResponse.setStatus(Loan.LoanStatus.REJECTED);
            when(loanService.rejectLoan(eq(1L), eq("Rejected due to policy violation")))
                    .thenReturn(loanResponse);

            mockMvc.perform(put("/loans/admin/1/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(STATUS_PATH).value("REJECTED"));
        }

        @Test
        @DisplayName("PUT /loans/admin/{id}/disburse - Success")
        void disburseLoanShouldSucceed() throws Exception {
            loanResponse.setStatus(Loan.LoanStatus.DISBURSED);
            when(loanService.disburseLoan(eq(1L))).thenReturn(loanResponse);

            mockMvc.perform(put("/loans/admin/1/disburse"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(STATUS_PATH).value("DISBURSED"));
        }
    }
}

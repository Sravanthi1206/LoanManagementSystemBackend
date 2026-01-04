package com.lms.emi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.emi.dto.EmiCalculationRequest;
import com.lms.emi.dto.EmiCalculationResponse;
import com.lms.emi.entity.RepaymentSchedule;
import com.lms.emi.service.EmiService;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EmiController Tests")
class EmiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmiService emiService;

    private EmiCalculationRequest calculationRequest;
    private EmiCalculationResponse calculationResponse;
    private RepaymentSchedule repaymentSchedule;

    @BeforeEach
    void setUp() {
        calculationRequest = new EmiCalculationRequest();
        calculationRequest.setPrincipalAmount(new BigDecimal("100000"));
        calculationRequest.setAnnualInterestRate(new BigDecimal("12"));
        calculationRequest.setTenureMonths(12);

        calculationResponse = EmiCalculationResponse.builder()
                .principalAmount(new BigDecimal("100000"))
                .annualInterestRate(new BigDecimal("12"))
                .tenureMonths(12)
                .monthlyEmi(new BigDecimal("8884.88"))
                .totalInterest(new BigDecimal("6618.56"))
                .totalPayment(new BigDecimal("106618.56"))
                .build();

        repaymentSchedule = RepaymentSchedule.builder()
                .id(1L)
                .loanId(1L)
                .installmentNo(1)
                .dueDate(LocalDate.now().plusMonths(1))
                .totalEmi(new BigDecimal("8884.88"))
                .principalAmount(new BigDecimal("7884.88"))
                .interestAmount(new BigDecimal("1000.00"))
                .status(RepaymentSchedule.PaymentStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Calculate EMI Tests")
    class CalculateEmiTests {

        @Test
        @DisplayName("POST /emi/calculate - Success")
        void calculateEmi_Success() throws Exception {
            when(emiService.calculateEmi(any(), any(), any()))
                .thenReturn(calculationResponse);

            mockMvc.perform(post("/emi/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(calculationRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.monthlyEmi").exists());
        }
    }

    @Nested
    @DisplayName("Generate Schedule Tests")
    class GenerateScheduleTests {

        @Test
        @DisplayName("POST /emi/generate - Success")
        void generateSchedule_Success() throws Exception {
            doNothing().when(emiService).generateSchedule(anyLong(), anyLong(), any(), any(), anyInt());

            mockMvc.perform(post("/emi/generate")
                    .param("loanId", "1")
                    .param("userId", "1")
                    .param("amount", "100000")
                    .param("rate", "12")
                    .param("tenure", "12"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Get Schedule Tests")
    class GetScheduleTests {

        @Test
        @DisplayName("GET /emi/schedule/{loanId} - Success")
        void getSchedule_Success() throws Exception {
            List<RepaymentSchedule> schedules = Collections.singletonList(repaymentSchedule);
            when(emiService.getSchedule(1L)).thenReturn(schedules);

            mockMvc.perform(get("/emi/schedule/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].loanId").value(1));
        }

        @Test
        @DisplayName("GET /emi/upcoming/{userId} - Success")
        void getUpcomingEmis_Success() throws Exception {
            List<RepaymentSchedule> schedules = Collections.singletonList(repaymentSchedule);
            when(emiService.getUpcomingEmis(1L)).thenReturn(schedules);

            mockMvc.perform(get("/emi/upcoming/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].loanId").value(1));
        }
    }

    @Nested
    @DisplayName("Mark Paid Tests")
    class MarkPaidTests {

        @Test
        @DisplayName("PUT /emi/installment/{id}/paid - Success")
        void markInstallmentPaid_Success() throws Exception {
            repaymentSchedule.setStatus(RepaymentSchedule.PaymentStatus.PAID);
            when(emiService.markInstallmentAsPaid(1L)).thenReturn(repaymentSchedule);

            mockMvc.perform(put("/emi/installment/1/paid"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAID"));
        }
    }
}

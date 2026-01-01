package com.lms.emi.service;

import com.lms.emi.dto.EmiCalculationRequest;
import com.lms.emi.dto.EmiCalculationResponse;
import com.lms.emi.entity.RepaymentSchedule;
import com.lms.emi.repository.RepaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmiService Unit Tests")
class EmiServiceTest {

    @Mock
    private RepaymentRepository scheduleRepository;

    @InjectMocks
    private EmiService emiService;

    private EmiCalculationRequest calculationRequest;

    @BeforeEach
    void setUp() {
        calculationRequest = new EmiCalculationRequest();
        calculationRequest.setPrincipalAmount(new BigDecimal("100000"));
        calculationRequest.setAnnualInterestRate(new BigDecimal("12"));
        calculationRequest.setTenureMonths(12);
    }

    @Test
    @DisplayName("Should calculate EMI correctly")
    void calculateEmi_Success() {
        // Act
        EmiCalculationResponse response = emiService.calculateEmi(
                calculationRequest.getPrincipalAmount(),
                calculationRequest.getAnnualInterestRate(),
                calculationRequest.getTenureMonths()
        );

        // Assert
        assertNotNull(response);
        assertNotNull(response.getMonthlyEmi());
        assertTrue(response.getMonthlyEmi().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(calculationRequest.getPrincipalAmount(), response.getPrincipalAmount());
        assertEquals(calculationRequest.getTenureMonths(), response.getTenureMonths());
    }

    @Test
    @DisplayName("Should calculate total interest correctly")
    void calculateEmi_TotalInterest() {
        // Act
        EmiCalculationResponse response = emiService.calculateEmi(
                calculationRequest.getPrincipalAmount(),
                calculationRequest.getAnnualInterestRate(),
                calculationRequest.getTenureMonths()
        );

        // Assert
        assertNotNull(response.getTotalInterest());
        assertTrue(response.getTotalInterest().compareTo(BigDecimal.ZERO) > 0);
        
        // Total payment should be principal + interest
        BigDecimal expectedTotal = calculationRequest.getPrincipalAmount()
                .add(response.getTotalInterest());
        assertEquals(0, expectedTotal.compareTo(response.getTotalPayment()));
    }

    @Test
    @DisplayName("Should generate repayment schedule for loan")
    void generateSchedule_Success() {
        // Arrange
        when(scheduleRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        emiService.generateSchedule(
                1L,
                new BigDecimal("100000"),
                new BigDecimal("12"),
                12
        );

        // Assert
        verify(scheduleRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should get schedule by loan ID")
    void getScheduleByLoanId_Success() {
        // Arrange
        RepaymentSchedule schedule1 = RepaymentSchedule.builder()
                .id(1L)
                .loanId(1L)
                .installmentNo(1)
                .dueDate(LocalDate.now().plusMonths(1))
                .totalEmi(new BigDecimal("8884.88"))
                .status(RepaymentSchedule.PaymentStatus.PENDING)
                .build();
        
        when(scheduleRepository.findByLoanId(1L))
                .thenReturn(Arrays.asList(schedule1));

        // Act
        List<RepaymentSchedule> result = emiService.getSchedule(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository).findByLoanId(1L);
    }

    @Test
    @DisplayName("Should mark installment as paid")
    void markInstallmentAsPaid_Success() {
        // Arrange
        RepaymentSchedule schedule = RepaymentSchedule.builder()
                .id(1L)
                .loanId(1L)
                .installmentNo(1)
                .status(RepaymentSchedule.PaymentStatus.PENDING)
                .build();
        
        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule));
        when(scheduleRepository.save(any(RepaymentSchedule.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        RepaymentSchedule result = emiService.markInstallmentAsPaid(1L);

        // Assert
        assertNotNull(result);
        assertEquals(RepaymentSchedule.PaymentStatus.PAID, result.getStatus());
        assertNotNull(result.getPaidDate());
    }

    @Test
    @DisplayName("Should calculate EMI using standard formula")
    void calculateEmi_FormulaValidation() {
        // Arrange - Known values for validation
        // Principal: 100000, Rate: 12% annual (1% monthly), Tenure: 12 months
        // Expected EMI ≈ 8884.88
        
        // Act
        EmiCalculationResponse response = emiService.calculateEmi(
                calculationRequest.getPrincipalAmount(),
                calculationRequest.getAnnualInterestRate(),
                calculationRequest.getTenureMonths()
        );

        // Assert
        // EMI should be approximately 8884.88 for these values
        BigDecimal expectedEmi = new BigDecimal("8884.88");
        assertTrue(response.getMonthlyEmi().subtract(expectedEmi).abs()
                .compareTo(new BigDecimal("1")) < 0,
                "EMI should be approximately 8884.88");
    }
}

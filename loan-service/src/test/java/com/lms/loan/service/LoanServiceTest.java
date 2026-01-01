package com.lms.loan.service;

import com.lms.loan.dto.LoanApplicationRequest;
import com.lms.loan.dto.LoanApplicationResponse;
import com.lms.loan.dto.LoanApprovalRequest;
import com.lms.loan.entity.Loan;
import com.lms.loan.repository.LoanRepository;
import com.lms.loan.messaging.NotificationPublisher;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService Unit Tests")
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private LoanService loanService;

    private Loan testLoan;
    private LoanApplicationRequest applicationRequest;

    @BeforeEach
    void setUp() {
        testLoan = Loan.builder()
                .loanId(1L)
                .userId(100L)
                .type(Loan.LoanType.PERSONAL)
                .amountRequested(new BigDecimal("100000"))
                .tenureMonths(12)
                .purpose("Home renovation")
                .employmentType(Loan.EmploymentType.SALARIED)
                .monthlyIncome(new BigDecimal("50000"))
                .status(Loan.LoanStatus.APPLIED)
                .appliedOn(LocalDateTime.now())
                .build();

        applicationRequest = new LoanApplicationRequest();
        applicationRequest.setUserId(100L);
        applicationRequest.setType(Loan.LoanType.PERSONAL);
        applicationRequest.setAmount(new BigDecimal("100000"));
        applicationRequest.setTenure(12);
        applicationRequest.setPurpose("Home renovation");
        applicationRequest.setEmploymentType(Loan.EmploymentType.SALARIED);
        applicationRequest.setMonthlyIncome(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should apply for loan successfully")
    void applyForLoan_Success() {
        // Arrange
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        // Act
        LoanApplicationResponse response = loanService.applyLoan(applicationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(Loan.LoanStatus.APPLIED, response.getStatus());
        verify(loanRepository).save(any(Loan.class));
        verify(notificationPublisher).sendLoanNotification(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should get loans by user ID")
    void getLoansByUserId_ReturnsLoans() {
        // Arrange
        List<Loan> loans = Arrays.asList(testLoan);
        when(loanRepository.findByUserId(100L)).thenReturn(loans);

        // Act
        List<LoanApplicationResponse> result = loanService.getMyLoans(100L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository).findByUserId(100L);
    }

    @Test
    @DisplayName("Should get loans by status with pagination")
    void getLoansByStatus_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(Arrays.asList(testLoan));
        when(loanRepository.findByStatus(eq(Loan.LoanStatus.APPLIED), any(Pageable.class)))
                .thenReturn(loanPage);

        // Act
        Page<LoanApplicationResponse> result = loanService.getLoansByStatus(Loan.LoanStatus.APPLIED, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(loanRepository).findByStatus(Loan.LoanStatus.APPLIED, pageable);
    }

    @Test
    @DisplayName("Should approve loan successfully")
    void approveLoan_Success() {
        // Arrange
        testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        LoanApprovalRequest approvalRequest = new LoanApprovalRequest();
        approvalRequest.setApprovedAmount(new BigDecimal("100000"));
        approvalRequest.setInterestRate(new BigDecimal("12.5"));
        approvalRequest.setRemarks("Good credit score");

        // Act
        LoanApplicationResponse response = loanService.approveLoan(1L, approvalRequest);

        // Assert
        assertNotNull(response);
        assertEquals(Loan.LoanStatus.APPROVED, response.getStatus());
        verify(notificationPublisher).sendLoanNotification(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reject loan with remarks")
    void rejectLoan_Success() {
        // Arrange
        testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        LoanApplicationResponse response = loanService.rejectLoan(1L, "Insufficient income");

        // Assert
        assertNotNull(response);
        assertEquals(Loan.LoanStatus.REJECTED, response.getStatus());
        verify(notificationPublisher).sendLoanNotification(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void getLoanById_NotFound_ThrowsException() {
        // Arrange
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> loanService.getLoanById(999L));
    }

    @Test
    @DisplayName("Should start loan review")
    void reviewLoan_Success() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        LoanApplicationResponse response = loanService.reviewLoan(1L, 50L);

        // Assert
        assertNotNull(response);
        assertEquals(Loan.LoanStatus.UNDER_REVIEW, response.getStatus());
    }
}

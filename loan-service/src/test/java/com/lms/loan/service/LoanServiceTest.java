package com.lms.loan.service;

import com.lms.loan.dto.*;
import com.lms.loan.entity.Loan;
import com.lms.loan.exception.InvalidLoanStatusException;
import com.lms.loan.exception.LoanNotFoundException;
import com.lms.loan.exception.UnauthorizedAccessException;
import com.lms.loan.messaging.NotificationPublisher;
import com.lms.loan.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for LoanService to achieve high code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService Tests")
class LoanServiceTest {

    @Mock
    private LoanRepository repository;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private com.lms.loan.client.EmiClient emiClient;

    @InjectMocks
    private LoanService loanService;

    private Loan testLoan;
    private LoanApplicationRequest applicationRequest;

    @BeforeEach
    void setUp() {
        testLoan = Loan.builder()
                .loanId(1L)
                .userId(1L)
                .type(Loan.LoanType.PERSONAL)
                .amountRequested(new BigDecimal("100000"))
                .purpose("Home Renovation")
                .tenureMonths(24)
                .status(Loan.LoanStatus.APPLIED)
                .employmentType(Loan.EmploymentType.SALARIED)
                .monthlyIncome(new BigDecimal("50000"))
                .annualIncome(new BigDecimal("600000"))
                .appliedOn(LocalDateTime.now())
                .build();

        applicationRequest = new LoanApplicationRequest();
        applicationRequest.setUserId(1L);
        applicationRequest.setType(Loan.LoanType.PERSONAL);
        applicationRequest.setAmount(new BigDecimal("100000"));
        applicationRequest.setPurpose("Home Renovation");
        applicationRequest.setTenure(24);
        applicationRequest.setEmploymentType(Loan.EmploymentType.SALARIED);
        applicationRequest.setMonthlyIncome(new BigDecimal("50000"));
        applicationRequest.setAnnualIncome(new BigDecimal("600000"));
    }

    @Nested
    @DisplayName("Loan Application Tests")
    class LoanApplicationTests {

        @Test
        @DisplayName("Should apply for loan successfully")
        void applyLoan_Success() {
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.applyLoan(applicationRequest);

            assertNotNull(result);
            assertEquals(testLoan.getLoanId(), result.getLoanId());
            assertEquals(Loan.LoanStatus.APPLIED, result.getStatus());
            verify(repository).save(any(Loan.class));
            verify(notificationPublisher).sendLoanNotification(eq(1L), eq(1L), eq("LOAN_APPLIED"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should apply for different loan types")
        void applyLoan_DifferentTypes() {
            for (Loan.LoanType type : Loan.LoanType.values()) {
                applicationRequest.setType(type);
                testLoan.setType(type);
                when(repository.save(any(Loan.class))).thenReturn(testLoan);

                LoanApplicationResponse result = loanService.applyLoan(applicationRequest);

                assertNotNull(result);
                assertEquals(type, result.getType());
            }
        }
    }

    @Nested
    @DisplayName("Get Loan Tests")
    class GetLoanTests {

        @Test
        @DisplayName("Should get loan by ID entity")
        void getLoan_Success() {
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));

            Loan result = loanService.getLoan(1L);

            assertNotNull(result);
            assertEquals(testLoan.getLoanId(), result.getLoanId());
        }

        @Test
        @DisplayName("Should throw exception when loan not found")
        void getLoan_NotFound_ThrowsException() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(LoanNotFoundException.class, () -> loanService.getLoan(999L));
        }

        @Test
        @DisplayName("Should get loan by ID with response")
        void getLoanById_Success() {
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));

            LoanApplicationResponse result = loanService.getLoanById(1L);

            assertNotNull(result);
            assertEquals(testLoan.getLoanId(), result.getLoanId());
        }

        @Test
        @DisplayName("Should get user's loans")
        void getMyLoans_Success() {
            List<Loan> loans = Collections.singletonList(testLoan);
            when(repository.findByUserId(1L)).thenReturn(loans);

            List<LoanApplicationResponse> result = loanService.getMyLoans(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list when no loans")
        void getMyLoans_NoLoans() {
            when(repository.findByUserId(999L)).thenReturn(Collections.emptyList());

            List<LoanApplicationResponse> result = loanService.getMyLoans(999L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Withdraw Loan Tests")
    class WithdrawLoanTests {

        @Test
        @DisplayName("Should withdraw loan successfully")
        void withdrawLoan_Success() {
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.withdrawLoan(1L, 1L);

            assertNotNull(result);
            verify(repository).save(argThat(loan -> loan.getStatus() == Loan.LoanStatus.WITHDRAWN));
        }

        @Test
        @DisplayName("Should throw exception when withdrawing others loan")
        void withdrawLoan_UnauthorizedAccess_ThrowsException() {
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));

            assertThrows(UnauthorizedAccessException.class, 
                () -> loanService.withdrawLoan(1L, 999L));
        }

        @Test
        @DisplayName("Should throw exception when loan not in APPLIED status")
        void withdrawLoan_InvalidStatus_ThrowsException() {
            testLoan.setStatus(Loan.LoanStatus.APPROVED);
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));

            assertThrows(InvalidLoanStatusException.class, 
                () -> loanService.withdrawLoan(1L, 1L));
        }
    }

    @Nested
    @DisplayName("Officer Operations Tests")
    class OfficerOperationsTests {

        @Test
        @DisplayName("Should get loans by status")
        void getLoansByStatus_Success() {
            Page<Loan> page = new PageImpl<>(Collections.singletonList(testLoan));
            when(repository.findByStatus(eq(Loan.LoanStatus.APPLIED), any(Pageable.class))).thenReturn(page);

            Page<LoanApplicationResponse> result = loanService.getLoansByStatus(
                Loan.LoanStatus.APPLIED, PageRequest.of(0, 10));

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should start review")
        void reviewLoan_Success() {
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.reviewLoan(1L, 100L);

            assertNotNull(result);
            verify(repository).save(argThat(loan -> loan.getStatus() == Loan.LoanStatus.UNDER_REVIEW));
            verify(repository).save(argThat(loan -> loan.getAssignedOfficerId() == 100L));
        }

        @Test
        @DisplayName("Should throw exception when loan already assigned to different officer")
        void reviewLoan_AlreadyAssigned_ThrowsException() {
            testLoan.setAssignedOfficerId(100L); // Already assigned to officer 100
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));

            assertThrows(IllegalStateException.class, 
                () -> loanService.reviewLoan(1L, 200L)); // Officer 200 tries to take it
        }

        @Test
        @DisplayName("Should allow same officer to review again")
        void reviewLoan_SameOfficer_Success() {
            testLoan.setAssignedOfficerId(100L); // Already assigned to officer 100
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.reviewLoan(1L, 100L); // Same officer

            assertNotNull(result);
            verify(repository).save(any(Loan.class));
        }

        @Test
        @DisplayName("Should approve loan")
        void approveLoan_Success() {
            testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            testLoan.setCreditScore(750); // Satisfy credit check requirement
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApprovalRequest request = new LoanApprovalRequest();
            request.setApprovedAmount(new BigDecimal("90000"));
            request.setInterestRate(new BigDecimal("12.5"));
            request.setRemarks("Approved");

            LoanApplicationResponse result = loanService.approveLoan(1L, request);

            assertNotNull(result);
            assertEquals(Loan.LoanStatus.APPROVED, result.getStatus());
            verify(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), eq("LOAN_APPROVED"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should reject loan")
        void rejectLoan_Success() {
            testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.rejectLoan(1L, "Low credit score");

            assertNotNull(result);
            assertEquals(Loan.LoanStatus.REJECTED, result.getStatus());
            verify(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), eq("LOAN_REJECTED"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should perform credit check with manual score")
        void performCreditCheck_Success() {
            testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.performCreditCheck(1L, 750, "Good");

            assertNotNull(result);
            verify(repository).save(any(Loan.class));
        }

        @Test
        @DisplayName("Should perform automated credit check when score is null")
        void performCreditCheck_Automated_Success() {
            testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            // Base 600 + Salaried 50 + Income 50 = 700
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.performCreditCheck(1L, null, "Auto Check");

            assertNotNull(result);
            verify(repository).save(argThat(loan -> 
                loan.getCreditScore() != null && 
                loan.getCreditScore() >= 300 && 
                loan.getCreditScore() <= 900
            ));
        }

        @Test
        @DisplayName("Should disburse loan")
        void disburseLoan_Success() {
            testLoan.setStatus(Loan.LoanStatus.APPROVED);
            testLoan.setAmountApproved(new BigDecimal("90000"));
            testLoan.setInterestRate(new BigDecimal("12.5"));
            testLoan.setTenureMonths(24);
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            LoanApplicationResponse result = loanService.disburseLoan(1L);

            assertNotNull(result);
            assertEquals(Loan.LoanStatus.DISBURSED, result.getStatus());
            verify(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), eq("LOAN_DISBURSED"), anyString(), anyString(), anyString());
            verify(emiClient).generateSchedule(anyLong(), anyLong(), any(BigDecimal.class), any(BigDecimal.class), anyInt());
        }

        @Test
        @DisplayName("Should throw exception when disbursing non-approved loan")
        void disburseLoan_InvalidStatus_ThrowsException() {
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));

            assertThrows(InvalidLoanStatusException.class, 
                () -> loanService.disburseLoan(1L));
        }
    }

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should transition from APPLIED to UNDER_REVIEW")
        void statusTransition_AppliedToUnderReview() {
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            loanService.reviewLoan(1L, 100L);

            verify(repository).save(argThat(loan -> 
                loan.getStatus() == Loan.LoanStatus.UNDER_REVIEW));
        }

        @Test
        @DisplayName("Should transition from APPROVED to DISBURSED")
        void statusTransition_ApprovedToDisbursed() {
            testLoan.setStatus(Loan.LoanStatus.APPROVED);
            testLoan.setAmountApproved(new BigDecimal("90000"));
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);

            loanService.disburseLoan(1L);

            verify(repository).save(argThat(loan -> 
                loan.getStatus() == Loan.LoanStatus.DISBURSED));
        }
    }
    @Nested
    @DisplayName("Robustness Tests")
    class RobustnessTests {

        @Test
        @DisplayName("Should save loan even if notification fails during application")
        void applyLoan_NotificationFail_ShouldStillSave() {
            // Arrange
            when(repository.save(any(Loan.class))).thenReturn(testLoan);
            doThrow(new RuntimeException("RabbitMQ Down"))
                .when(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());

            // Act
            LoanApplicationResponse result = loanService.applyLoan(applicationRequest);

            // Assert
            assertNotNull(result);
            verify(repository).save(any(Loan.class)); // Verifies DB save persisted
            verify(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should save approval even if notification fails")
        void approveLoan_NotificationFail_ShouldStillSave() {
            // Arrange
            testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            testLoan.setCreditScore(750);
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);
            doThrow(new RuntimeException("RabbitMQ Down"))
                .when(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());

            LoanApprovalRequest request = new LoanApprovalRequest();
            request.setApprovedAmount(new BigDecimal("90000"));
            request.setInterestRate(new BigDecimal("12.5"));
            request.setRemarks("Approved");

            // Act
            LoanApplicationResponse result = loanService.approveLoan(1L, request);

            // Assert
            assertNotNull(result);
            assertEquals(Loan.LoanStatus.APPROVED, result.getStatus());
            verify(repository).save(any(Loan.class));
        }

        @Test
        @DisplayName("Should save rejection even if notification fails")
        void rejectLoan_NotificationFail_ShouldStillSave() {
            // Arrange
            testLoan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);
            doThrow(new RuntimeException("RabbitMQ Down"))
                .when(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());

            // Act
            LoanApplicationResponse result = loanService.rejectLoan(1L, "Rejected");

            // Assert
            assertNotNull(result);
            assertEquals(Loan.LoanStatus.REJECTED, result.getStatus());
            verify(repository).save(any(Loan.class));
        }

        @Test
        @DisplayName("Should save disbursement even if notification fails")
        void disburseLoan_NotificationFail_ShouldStillSave() {
            // Arrange
            testLoan.setStatus(Loan.LoanStatus.APPROVED);
            testLoan.setAmountApproved(new BigDecimal("90000"));
            testLoan.setInterestRate(new BigDecimal("12.5"));
            testLoan.setTenureMonths(24);
            when(repository.findById(1L)).thenReturn(Optional.of(testLoan));
            when(repository.save(any(Loan.class))).thenReturn(testLoan);
            doThrow(new RuntimeException("RabbitMQ Down"))
                .when(notificationPublisher).sendLoanNotification(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());

            // Act
            LoanApplicationResponse result = loanService.disburseLoan(1L);

            // Assert
            assertNotNull(result);
            assertEquals(Loan.LoanStatus.DISBURSED, result.getStatus());
            verify(repository).save(any(Loan.class));
        }
    }
}

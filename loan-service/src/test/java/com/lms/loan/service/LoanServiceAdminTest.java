package com.lms.loan.service;

import com.lms.loan.entity.Loan;
import com.lms.loan.exception.InvalidLoanStatusException;
import com.lms.loan.exception.LoanNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService Admin Methods Tests")
class LoanServiceAdminTest {

    @Mock
    private LoanRepository loans;

    @Mock
    private NotificationPublisher notifications;

    @Mock
    private com.lms.loan.client.EmiClient emiClient;

    @InjectMocks
    private LoanService loanService;

    private Loan testLoan;

    @BeforeEach
    void setUp() {
        testLoan = Loan.builder()
                .loanId(1L)
                .userId(100L)
                .userEmail("test@example.com")
                .type(Loan.LoanType.PERSONAL)
                .amountRequested(new BigDecimal("50000"))
                .tenureMonths(12)
                .status(Loan.LoanStatus.UNDER_REVIEW)
                .assignedOfficerId(200L)
                .assignedAt(LocalDateTime.now().minusHours(10))
                .build();
    }

    @Nested
    @DisplayName("Reassign Loan Tests")
    class ReassignLoanTests {

        @Test
        @DisplayName("Should reassign loan to new officer")
        void reassignLoan_Success() {
            when(loans.findById(1L)).thenReturn(Optional.of(testLoan));
            when(loans.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

            var result = loanService.reassignLoan(1L, 300L);

            assertNotNull(result);
            verify(loans).save(argThat(loan -> 
                loan.getAssignedOfficerId().equals(300L) &&
                loan.getAssignedAt() != null &&
                loan.getStatus() == Loan.LoanStatus.UNDER_REVIEW
            ));
        }

        @Test
        @DisplayName("Should throw when loan not found")
        void reassignLoan_LoanNotFound() {
            when(loans.findById(999L)).thenReturn(Optional.empty());

            assertThrows(LoanNotFoundException.class, () -> 
                loanService.reassignLoan(999L, 300L));
        }

        @Test
        @DisplayName("Should throw when loan status is invalid for reassignment")
        void reassignLoan_InvalidStatus() {
            testLoan.setStatus(Loan.LoanStatus.DISBURSED);
            when(loans.findById(1L)).thenReturn(Optional.of(testLoan));

            assertThrows(InvalidLoanStatusException.class, () -> 
                loanService.reassignLoan(1L, 300L));
        }
    }

    @Nested
    @DisplayName("Release Loan Tests")
    class ReleaseLoanTests {

        @Test
        @DisplayName("Should release loan back to pool")
        void releaseLoan_Success() {
            when(loans.findById(1L)).thenReturn(Optional.of(testLoan));
            when(loans.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

            var result = loanService.releaseLoan(1L);

            assertNotNull(result);
            verify(loans).save(argThat(loan -> 
                loan.getAssignedOfficerId() == null &&
                loan.getAssignedAt() == null &&
                loan.getStatus() == Loan.LoanStatus.APPLIED
            ));
        }

        @Test
        @DisplayName("Should throw when loan is not UNDER_REVIEW")
        void releaseLoan_InvalidStatus() {
            testLoan.setStatus(Loan.LoanStatus.APPROVED);
            when(loans.findById(1L)).thenReturn(Optional.of(testLoan));

            assertThrows(InvalidLoanStatusException.class, () -> 
                loanService.releaseLoan(1L));
        }
    }

    @Nested
    @DisplayName("Officer Workload Tests")
    class OfficerWorkloadTests {

        @Test
        @DisplayName("Should return pending loan count for officer")
        void getOfficerPendingCount_Success() {
            when(loans.countByAssignedOfficerIdAndStatus(200L, Loan.LoanStatus.UNDER_REVIEW))
                    .thenReturn(5L);

            long count = loanService.getOfficerPendingCount(200L);

            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("Release Stale Loans Tests")
    class ReleaseStaleLoanTests {

        @Test
        @DisplayName("Should release loans older than timeout")
        void releaseStaleLoans_Success() {
            Loan staleLoan1 = Loan.builder()
                    .loanId(1L)
                    .status(Loan.LoanStatus.UNDER_REVIEW)
                    .assignedOfficerId(100L)
                    .assignedAt(LocalDateTime.now().minusHours(40))
                    .build();
            Loan staleLoan2 = Loan.builder()
                    .loanId(2L)
                    .status(Loan.LoanStatus.UNDER_REVIEW)
                    .assignedOfficerId(100L)
                    .assignedAt(LocalDateTime.now().minusHours(50))
                    .build();

            when(loans.findStaleAssignedLoans(eq(Loan.LoanStatus.UNDER_REVIEW), any(LocalDateTime.class)))
                    .thenReturn(List.of(staleLoan1, staleLoan2));
            when(loans.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

            int released = loanService.releaseStaleLoans(36);

            assertEquals(2, released);
            verify(loans, times(2)).save(any(Loan.class));
        }

        @Test
        @DisplayName("Should return 0 when no stale loans")
        void releaseStaleLoans_NoStaleLoans() {
            when(loans.findStaleAssignedLoans(eq(Loan.LoanStatus.UNDER_REVIEW), any(LocalDateTime.class)))
                    .thenReturn(List.of());

            int released = loanService.releaseStaleLoans(36);

            assertEquals(0, released);
            verify(loans, never()).save(any(Loan.class));
        }
    }
}

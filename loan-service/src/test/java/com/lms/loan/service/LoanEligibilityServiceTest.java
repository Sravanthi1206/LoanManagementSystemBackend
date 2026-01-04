package com.lms.loan.service;

import com.lms.loan.dto.LoanApplicationRequest;
import com.lms.loan.entity.Loan;
import com.lms.loan.exception.LoanEligibilityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoanEligibilityService - Complex Business Rules Tests")
class LoanEligibilityServiceTest {

    private LoanEligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new LoanEligibilityService();
    }

    // ========================================
    // Credit Score-Based Loan Limits Tests
    // ========================================
    @Nested
    @DisplayName("Credit Score-Based Loan Limits")
    class CreditScoreLoanLimitsTests {

        @Test
        @DisplayName("Excellent credit (750+) gets max ₹25 Lakhs limit")
        void excellentCredit_GetsMaxLoanLimit() {
            BigDecimal maxLoan = eligibilityService.getMaxLoanAmountByCredit(780);
            assertEquals(new BigDecimal("2500000"), maxLoan);
        }

        @Test
        @DisplayName("Good credit (700-749) gets max ₹15 Lakhs limit")
        void goodCredit_GetsReducedLimit() {
            BigDecimal maxLoan = eligibilityService.getMaxLoanAmountByCredit(720);
            assertEquals(new BigDecimal("1500000"), maxLoan);
        }

        @Test
        @DisplayName("Fair credit (650-699) gets max ₹5 Lakhs limit")
        void fairCredit_GetsLimitedAmount() {
            BigDecimal maxLoan = eligibilityService.getMaxLoanAmountByCredit(670);
            assertEquals(new BigDecimal("500000"), maxLoan);
        }

        @Test
        @DisplayName("Minimum credit (600-649) gets max ₹1 Lakh limit")
        void minimumCredit_GetsMinimumLimit() {
            BigDecimal maxLoan = eligibilityService.getMaxLoanAmountByCredit(620);
            assertEquals(new BigDecimal("100000"), maxLoan);
        }
    }

    // ========================================
    // Interest Rate Calculation Tests
    // ========================================
    @Nested
    @DisplayName("Dynamic Interest Rate Calculation")
    class InterestRateCalculationTests {

        @Test
        @DisplayName("Excellent credit gets 9% interest rate")
        void excellentCredit_GetsLowestRate() {
            BigDecimal rate = eligibilityService.getInterestRateByCredit(780);
            assertEquals(new BigDecimal("9.0"), rate);
        }

        @Test
        @DisplayName("Good credit gets 10% interest rate")
        void goodCredit_GetsReducedRate() {
            BigDecimal rate = eligibilityService.getInterestRateByCredit(720);
            assertEquals(new BigDecimal("10.0"), rate);
        }

        @Test
        @DisplayName("Fair credit gets 11% interest rate")
        void fairCredit_GetsMediumRate() {
            BigDecimal rate = eligibilityService.getInterestRateByCredit(670);
            assertEquals(new BigDecimal("11.0"), rate);
        }

        @Test
        @DisplayName("Minimum credit gets 12% base rate")
        void minimumCredit_GetsBaseRate() {
            BigDecimal rate = eligibilityService.getInterestRateByCredit(620);
            assertEquals(new BigDecimal("12.0"), rate);
        }
    }

    // ========================================
    // EMI Calculation Tests
    // ========================================
    @Nested
    @DisplayName("EMI Calculation")
    class EmiCalculationTests {

        @Test
        @DisplayName("Calculates correct EMI for standard loan")
        void calculatesCorrectEmi() {
            // ₹100,000 at 12% for 12 months
            BigDecimal emi = eligibilityService.calculateEstimatedEmi(
                    new BigDecimal("100000"), 
                    new BigDecimal("12.0"), 
                    12);
            
            // Expected EMI approximately ₹8,885
            assertTrue(emi.compareTo(new BigDecimal("8800")) > 0);
            assertTrue(emi.compareTo(new BigDecimal("9000")) < 0);
        }
    }

    // ========================================
    // Eligibility Validation Tests
    // ========================================
    @Nested
    @DisplayName("Loan Eligibility Validation")
    class EligibilityValidationTests {

        private LoanApplicationRequest createValidRequest() {
            LoanApplicationRequest request = new LoanApplicationRequest();
            request.setUserId(1L);
            request.setAmount(new BigDecimal("500000"));
            request.setTenure(24);
            request.setMonthlyIncome(new BigDecimal("100000"));
            request.setAnnualIncome(new BigDecimal("1200000"));
            request.setExistingEmiAmount(BigDecimal.ZERO);
            request.setType(Loan.LoanType.PERSONAL);
            return request;
        }

        @Test
        @DisplayName("Valid application passes all checks")
        void validApplication_PassesAllChecks() {
            LoanApplicationRequest request = createValidRequest();
            assertDoesNotThrow(() -> eligibilityService.validateEligibility(request, 750));
        }

        @Test
        @DisplayName("Low credit score fails validation")
        void lowCreditScore_FailsValidation() {
            LoanApplicationRequest request = createValidRequest();
            LoanEligibilityException ex = assertThrows(
                    LoanEligibilityException.class,
                    () -> eligibilityService.validateEligibility(request, 550));
            assertTrue(ex.getMessage().contains("Credit score"));
        }

        @Test
        @DisplayName("Low income fails validation")
        void lowIncome_FailsValidation() {
            LoanApplicationRequest request = createValidRequest();
            request.setMonthlyIncome(new BigDecimal("20000"));
            
            LoanEligibilityException ex = assertThrows(
                    LoanEligibilityException.class,
                    () -> eligibilityService.validateEligibility(request, 750));
            assertTrue(ex.getMessage().contains("Monthly income"));
        }

        @Test
        @DisplayName("Loan exceeding credit limit fails validation")
        void loanExceedingCreditLimit_FailsValidation() {
            LoanApplicationRequest request = createValidRequest();
            request.setAmount(new BigDecimal("3000000")); // 30 Lakhs
            
            LoanEligibilityException ex = assertThrows(
                    LoanEligibilityException.class,
                    () -> eligibilityService.validateEligibility(request, 750));
            assertTrue(ex.getMessage().contains("exceeds maximum"));
        }

        @Test
        @DisplayName("High EMI-to-income ratio fails validation")
        void highEmiToIncomeRatio_FailsValidation() {
            LoanApplicationRequest request = createValidRequest();
            request.setAmount(new BigDecimal("2000000"));
            request.setTenure(12); // Short tenure = high EMI
            request.setMonthlyIncome(new BigDecimal("50000"));
            
            LoanEligibilityException ex = assertThrows(
                    LoanEligibilityException.class,
                    () -> eligibilityService.validateEligibility(request, 750));
            assertTrue(ex.getMessage().contains("40%"));
        }

        @Test
        @DisplayName("High debt-to-income ratio fails validation")
        void highDebtToIncomeRatio_FailsValidation() {
            LoanApplicationRequest request = createValidRequest();
            request.setExistingEmiAmount(new BigDecimal("40000")); // Already 40% of income
            request.setMonthlyIncome(new BigDecimal("100000"));
            
            LoanEligibilityException ex = assertThrows(
                    LoanEligibilityException.class,
                    () -> eligibilityService.validateEligibility(request, 750));
            assertTrue(ex.getMessage().contains("50%"));
        }
    }

    // ========================================
    // Eligibility Summary Tests
    // ========================================
    @Nested
    @DisplayName("Eligibility Summary")
    class EligibilitySummaryTests {

        @Test
        @DisplayName("Returns correct summary for excellent credit")
        void excellentCredit_ReturnsCorrectSummary() {
            String summary = eligibilityService.getEligibilitySummary(780);
            assertTrue(summary.contains("EXCELLENT"));
            assertTrue(summary.contains("2500000"));
            assertTrue(summary.contains("9.0%"));
        }

        @Test
        @DisplayName("Returns ineligible for very low credit")
        void veryLowCredit_ReturnsIneligible() {
            String summary = eligibilityService.getEligibilitySummary(500);
            assertTrue(summary.contains("INELIGIBLE"));
        }
    }
}

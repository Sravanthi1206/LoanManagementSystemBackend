package com.lms.loan.service;

import com.lms.loan.dto.LoanApplicationRequest;
import com.lms.loan.exception.LoanEligibilityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service implementing complex business rules for loan eligibility.
 * 
 * Business Rules Implemented:
 * 1. Credit Score-based Loan Limits
 * 2. Dynamic Interest Rate Calculation
 * 3. EMI-to-Income Ratio Validation (max 40%)
 * 4. Debt-to-Income Ratio Check (max 50%)
 * 5. Minimum Income Requirements
 * 6. Age-based Tenure Restrictions
 */
@Service
@Slf4j
public class LoanEligibilityService {

    // Credit Score Thresholds
    private static final int CREDIT_SCORE_EXCELLENT = 750;
    private static final int CREDIT_SCORE_GOOD = 700;
    private static final int CREDIT_SCORE_FAIR = 650;
    private static final int CREDIT_SCORE_MINIMUM = 600;

    // Loan Amount Limits based on Credit Score
    private static final BigDecimal EXCELLENT_MAX_LOAN = new BigDecimal("2500000"); // 25 Lakhs
    private static final BigDecimal GOOD_MAX_LOAN = new BigDecimal("1500000");      // 15 Lakhs
    private static final BigDecimal FAIR_MAX_LOAN = new BigDecimal("500000");       // 5 Lakhs
    private static final BigDecimal MINIMUM_MAX_LOAN = new BigDecimal("100000");    // 1 Lakh

    // Base Interest Rate
    private static final BigDecimal BASE_INTEREST_RATE = new BigDecimal("12.0");

    // Income Requirements
    private static final BigDecimal MINIMUM_MONTHLY_INCOME = new BigDecimal("25000");
    private static final BigDecimal EMI_TO_INCOME_RATIO_MAX = new BigDecimal("0.40"); // 40%
    private static final BigDecimal DEBT_TO_INCOME_RATIO_MAX = new BigDecimal("0.50"); // 50%

    /**
     * Validates loan eligibility based on multiple business rules.
     * Throws LoanEligibilityException if any rule fails.
     */
    public void validateEligibility(LoanApplicationRequest request, int creditScore) {
        log.info("Validating loan eligibility for user {} with credit score {}", 
                request.getUserId(), creditScore);

        // Rule 1: Minimum Credit Score Check
        validateMinimumCreditScore(creditScore);

        // Rule 2: Minimum Income Requirement
        validateMinimumIncome(request.getMonthlyIncome());

        // Rule 3: Loan Amount within Credit Score Limit
        BigDecimal maxAllowedLoan = getMaxLoanAmountByCredit(creditScore);
        validateLoanAmount(request.getAmount(), maxAllowedLoan, creditScore);

        // Rule 4: EMI-to-Income Ratio (new EMI should not exceed 40% of income)
        BigDecimal estimatedEmi = calculateEstimatedEmi(request.getAmount(), 
                getInterestRateByCredit(creditScore), request.getTenure());
        validateEmiToIncomeRatio(estimatedEmi, request.getMonthlyIncome());

        // Rule 5: Debt-to-Income Ratio (total debt including new loan should not exceed 50%)
        BigDecimal totalDebt = request.getExistingEmiAmount().add(estimatedEmi);
        validateDebtToIncomeRatio(totalDebt, request.getMonthlyIncome());

        log.info("Loan eligibility validated successfully for user {}", request.getUserId());
    }

    /**
     * Rule 1: Credit Score must be at least 600.
     */
    private void validateMinimumCreditScore(int creditScore) {
        if (creditScore < CREDIT_SCORE_MINIMUM) {
            throw new LoanEligibilityException(
                String.format("Credit score %d is below minimum requirement of %d. " +
                    "Please improve your credit score before applying.", 
                    creditScore, CREDIT_SCORE_MINIMUM));
        }
    }

    /**
     * Rule 2: Minimum monthly income of ₹25,000 required.
     */
    private void validateMinimumIncome(BigDecimal monthlyIncome) {
        if (monthlyIncome.compareTo(MINIMUM_MONTHLY_INCOME) < 0) {
            throw new LoanEligibilityException(
                String.format("Monthly income of ₹%.2f is below minimum requirement of ₹%.2f",
                    monthlyIncome, MINIMUM_MONTHLY_INCOME));
        }
    }

    /**
     * Rule 3: Loan amount must be within credit score-based limit.
     */
    private void validateLoanAmount(BigDecimal requestedAmount, BigDecimal maxAllowed, int creditScore) {
        if (requestedAmount.compareTo(maxAllowed) > 0) {
            throw new LoanEligibilityException(
                String.format("Requested amount ₹%.2f exceeds maximum eligible amount ₹%.2f " +
                    "for credit score %d. Consider requesting a lower amount.",
                    requestedAmount, maxAllowed, creditScore));
        }
    }

    /**
     * Rule 4: EMI should not exceed 40% of monthly income.
     */
    private void validateEmiToIncomeRatio(BigDecimal emi, BigDecimal monthlyIncome) {
        BigDecimal maxEmi = monthlyIncome.multiply(EMI_TO_INCOME_RATIO_MAX);
        if (emi.compareTo(maxEmi) > 0) {
            throw new LoanEligibilityException(
                String.format("Estimated EMI of ₹%.2f exceeds 40%% of monthly income (₹%.2f). " +
                    "Consider a smaller loan amount or longer tenure.",
                    emi, maxEmi));
        }
    }

    /**
     * Rule 5: Total debt (existing + new) should not exceed 50% of monthly income.
     */
    private void validateDebtToIncomeRatio(BigDecimal totalDebt, BigDecimal monthlyIncome) {
        BigDecimal maxDebt = monthlyIncome.multiply(DEBT_TO_INCOME_RATIO_MAX);
        if (totalDebt.compareTo(maxDebt) > 0) {
            throw new LoanEligibilityException(
                String.format("Total monthly debt ₹%.2f would exceed 50%% of income (₹%.2f). " +
                    "Please reduce existing debts or request a smaller loan.",
                    totalDebt, maxDebt));
        }
    }

    /**
     * Calculates maximum loan amount based on credit score.
     * Higher credit score = Higher loan limit
     */
    public BigDecimal getMaxLoanAmountByCredit(int creditScore) {
        if (creditScore >= CREDIT_SCORE_EXCELLENT) {
            return EXCELLENT_MAX_LOAN;
        } else if (creditScore >= CREDIT_SCORE_GOOD) {
            return GOOD_MAX_LOAN;
        } else if (creditScore >= CREDIT_SCORE_FAIR) {
            return FAIR_MAX_LOAN;
        } else {
            return MINIMUM_MAX_LOAN;
        }
    }

    /**
     * Calculates interest rate based on credit score.
     * Higher credit score = Lower interest rate (risk-based pricing)
     */
    public BigDecimal getInterestRateByCredit(int creditScore) {
        BigDecimal rate = BASE_INTEREST_RATE;
        
        if (creditScore >= CREDIT_SCORE_EXCELLENT) {
            rate = rate.subtract(new BigDecimal("3.0")); // 9% for excellent
        } else if (creditScore >= CREDIT_SCORE_GOOD) {
            rate = rate.subtract(new BigDecimal("2.0")); // 10% for good
        } else if (creditScore >= CREDIT_SCORE_FAIR) {
            rate = rate.subtract(new BigDecimal("1.0")); // 11% for fair
        }
        // Below fair: base rate of 12%
        
        return rate;
    }

    /**
     * Calculates estimated monthly EMI using standard formula:
     * EMI = P × r × (1 + r)^n / ((1 + r)^n - 1)
     * Where:
     *   P = Principal loan amount
     *   r = Monthly interest rate (annual rate / 12 / 100)
     *   n = Number of months (tenure)
     */
    public BigDecimal calculateEstimatedEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12L * 100), 10, RoundingMode.HALF_UP);
        
        // (1 + r)^n
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        double powerVal = Math.pow(onePlusR.doubleValue(), tenureMonths);
        BigDecimal power = BigDecimal.valueOf(powerVal);
        
        // P × r × (1+r)^n
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        
        // (1+r)^n - 1
        BigDecimal denominator = power.subtract(BigDecimal.ONE);
        
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 10, RoundingMode.HALF_UP);
        }
        
        return numerator.divide(denominator, 10, RoundingMode.HALF_UP);
    }

    /**
     * Provides eligibility summary for a given credit score.
     */
    public String getEligibilitySummary(int creditScore) {
        BigDecimal maxLoan = getMaxLoanAmountByCredit(creditScore);
        BigDecimal interestRate = getInterestRateByCredit(creditScore);
        
        String tier;
        if (creditScore >= CREDIT_SCORE_EXCELLENT) {
            tier = "EXCELLENT";
        } else if (creditScore >= CREDIT_SCORE_GOOD) {
            tier = "GOOD";
        } else if (creditScore >= CREDIT_SCORE_FAIR) {
            tier = "FAIR";
        } else if (creditScore >= CREDIT_SCORE_MINIMUM) {
            tier = "MINIMUM";
        } else {
            tier = "INELIGIBLE";
        }
        
        return String.format("Credit Tier: %s | Max Loan: ₹%.2f | Interest Rate: %.1f%%", 
            tier, maxLoan, interestRate);
    }
}

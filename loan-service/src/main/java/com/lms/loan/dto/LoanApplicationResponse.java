package com.lms.loan.dto;

import com.lms.loan.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationResponse {
    private Long loanId;
    private Long userId;
    private Loan.LoanType type;
    private BigDecimal amountRequested;
    private Integer tenureMonths;
    private String purpose;
    private Loan.EmploymentType employmentType;
    private String employerName;
    private BigDecimal monthlyIncome;
    private BigDecimal annualIncome;
    private Boolean existingLoans;
    private BigDecimal existingEmiAmount;
    private BigDecimal interestRate;
    private BigDecimal amountApproved;
    private Loan.LoanStatus status;
    private String officerRemarks;
    private LocalDateTime appliedOn;
    private LocalDateTime approvedOn;
    private Integer creditScore;
    private Loan.RiskCategory riskCategory;
    private Long assignedOfficerId;
    private String assignedOfficerName;
}

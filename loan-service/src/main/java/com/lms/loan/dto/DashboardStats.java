package com.lms.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for Dashboard statistics and reports.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {
    
    // Loan Statistics
    private Long totalLoans;
    private Long appliedLoans;
    private Long underReviewLoans;
    private Long approvedLoans;
    private Long rejectedLoans;
    private Long disbursedLoans;
    private Long activeLoans;
    private Long closedLoans;
    
    // Financial Summary
    private BigDecimal totalDisbursedAmount;
    private BigDecimal totalOutstandingAmount;
    private BigDecimal totalCollectedAmount;
    
    // Loan Type Breakdown
    private Map<String, Long> loansByType;
    
    // Monthly Trends (optional)
    private Map<String, Long> monthlyApplications;
}

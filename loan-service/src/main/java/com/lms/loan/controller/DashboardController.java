package com.lms.loan.controller;

import com.lms.loan.dto.DashboardStats;
import com.lms.loan.entity.Loan;
import com.lms.loan.repository.LoanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard & Reports Controller for Admin and Loan Officer.
 * Provides aggregated statistics and reports for the loan management system.
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard & Reports", description = "Dashboard statistics and loan reports")
public class DashboardController {
    
    private final LoanRepository loanRepository;
    
    /**
     * Get overall dashboard statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Returns overall loan statistics for admin dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        List<Loan> allLoans = loanRepository.findAll();
        
        // Count by status using Java 8 Streams
        Map<Loan.LoanStatus, Long> statusCounts = allLoans.stream()
                .collect(Collectors.groupingBy(Loan::getStatus, Collectors.counting()));
        
        // Count by loan type using Java 8 Streams
        Map<String, Long> typeCounts = allLoans.stream()
                .collect(Collectors.groupingBy(
                        loan -> loan.getType().name(), 
                        Collectors.counting()
                ));
        
        // Calculate financial totals using Java 8 Streams
        BigDecimal totalDisbursed = allLoans.stream()
                .filter(loan -> loan.getAmountApproved() != null)
                .filter(loan -> loan.getStatus() == Loan.LoanStatus.DISBURSED || 
                               loan.getStatus() == Loan.LoanStatus.CLOSED)
                .map(Loan::getAmountApproved)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        DashboardStats stats = DashboardStats.builder()
                .totalLoans((long) allLoans.size())
                .appliedLoans(statusCounts.getOrDefault(Loan.LoanStatus.APPLIED, 0L))
                .underReviewLoans(statusCounts.getOrDefault(Loan.LoanStatus.UNDER_REVIEW, 0L))
                .approvedLoans(statusCounts.getOrDefault(Loan.LoanStatus.APPROVED, 0L) + 
                               statusCounts.getOrDefault(Loan.LoanStatus.DISBURSED, 0L) + 
                               statusCounts.getOrDefault(Loan.LoanStatus.CLOSED, 0L))
                .rejectedLoans(statusCounts.getOrDefault(Loan.LoanStatus.REJECTED, 0L))
                .disbursedLoans(statusCounts.getOrDefault(Loan.LoanStatus.DISBURSED, 0L))
                .activeLoans(statusCounts.getOrDefault(Loan.LoanStatus.DISBURSED, 0L)) // DISBURSED = active loans
                .closedLoans(statusCounts.getOrDefault(Loan.LoanStatus.CLOSED, 0L))
                .totalDisbursedAmount(totalDisbursed)
                .loansByType(typeCounts)
                .build();
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get loans count by status for chart display.
     */
    @GetMapping("/loans-by-status")
    @Operation(summary = "Get loans by status", description = "Returns loan count grouped by status for charts")
    public ResponseEntity<Map<String, Long>> getLoansByStatus() {
        List<Loan> allLoans = loanRepository.findAll();
        
        Map<String, Long> statusCounts = allLoans.stream()
                .collect(Collectors.groupingBy(
                        loan -> loan.getStatus().name(),
                        Collectors.counting()
                ));
        
        return ResponseEntity.ok(statusCounts);
    }
    
    /**
     * Get loans count by type for chart display.
     */
    @GetMapping("/loans-by-type")
    @Operation(summary = "Get loans by type", description = "Returns loan count grouped by loan type for charts")
    public ResponseEntity<Map<String, Long>> getLoansByType() {
        List<Loan> allLoans = loanRepository.findAll();
        
        Map<String, Long> typeCounts = allLoans.stream()
                .collect(Collectors.groupingBy(
                        loan -> loan.getType().name(),
                        Collectors.counting()
                ));
        
        return ResponseEntity.ok(typeCounts);
    }
    
    /**
     * Get customer-wise loan summary.
     */
    @GetMapping("/customer-summary/{userId}")
    @Operation(summary = "Get customer loan summary", description = "Returns loan summary for a specific customer")
    public ResponseEntity<Map<String, Object>> getCustomerSummary(@PathVariable Long userId) {
        List<Loan> userLoans = loanRepository.findByUserId(userId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLoans", userLoans.size());
        summary.put("activeLoans", userLoans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.DISBURSED)
                .count());
        summary.put("totalApproved", userLoans.stream()
                .filter(l -> l.getAmountApproved() != null)
                .map(Loan::getAmountApproved)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.put("loansByStatus", userLoans.stream()
                .collect(Collectors.groupingBy(
                        loan -> loan.getStatus().name(),
                        Collectors.counting()
                )));
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get overdue EMI report (placeholder - actual implementation in EMI service).
     */
    @GetMapping("/overdue-report")
    @Operation(summary = "Get overdue EMI report", description = "Returns list of overdue EMI payments")
    public ResponseEntity<Map<String, Object>> getOverdueReport() {
        // This would typically call EMI service to get overdue EMIs
        // For now, return a placeholder structure
        Map<String, Object> report = new HashMap<>();
        report.put("message", "Call EMI service at /api/emi/overdue for detailed overdue report");
        report.put("totalOverdue", 0);
        return ResponseEntity.ok(report);
    }
}

package com.lms.loan.service;

import com.lms.loan.dto.LoanApplicationRequest;
import com.lms.loan.dto.LoanApplicationResponse;
import com.lms.loan.dto.LoanApprovalRequest;
import com.lms.loan.entity.Loan;
import com.lms.loan.exception.InvalidLoanStatusException;
import com.lms.loan.exception.LoanNotFoundException;
import com.lms.loan.exception.UnauthorizedAccessException;
import com.lms.loan.messaging.NotificationPublisher;
import com.lms.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loans;
    private final NotificationPublisher notifications;
    private final com.lms.loan.client.EmiClient emiClient;

    // ... (existing code)

    @Transactional
    public LoanApplicationResponse applyLoan(LoanApplicationRequest req) {
        var loan = Loan.builder()
                .userId(req.getUserId())
                .type(req.getType())
                .amountRequested(req.getAmount())
                .tenureMonths(req.getTenure())
                .purpose(req.getPurpose())
                .employmentType(req.getEmploymentType())
                .employerName(req.getEmployerName())
                .monthlyIncome(req.getMonthlyIncome())
                .annualIncome(req.getAnnualIncome())
                .existingLoans(req.getExistingLoans())
                .existingEmiAmount(req.getExistingEmiAmount())
                .status(Loan.LoanStatus.APPLIED)
                .build();
        
        loan = loans.save(loan);
        notify(loan, "LOAN_APPLIED", "Application Received", 
               "Your loan #" + loan.getLoanId() + " for Rs." + loan.getAmountRequested() + " is under review.");
        return toResponse(loan);
    }

    public List<LoanApplicationResponse> getMyLoans(Long userId) {
        return loans.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public LoanApplicationResponse getLoanById(Long id) {
        return toResponse(findLoan(id));
    }

    public Loan getLoan(Long id) {
        return findLoan(id);
    }

    @Transactional
    public LoanApplicationResponse withdrawLoan(Long loanId, Long userId) {
        var loan = findLoan(loanId);
        if (!loan.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only withdraw your own applications");
        }
        requireStatus(loan, Loan.LoanStatus.APPLIED, "withdraw");
        loan.setStatus(Loan.LoanStatus.WITHDRAWN);
        return toResponse(loans.save(loan));
    }

    public Page<LoanApplicationResponse> getLoansByStatus(Loan.LoanStatus status, Pageable p) {
        return loans.findByStatus(status, p).map(this::toResponse);
    }

    public Page<LoanApplicationResponse> getAllLoans(Pageable p) {
        return loans.findAll(p).map(this::toResponse);
    }

    @Transactional
    public LoanApplicationResponse reviewLoan(Long id, Long officerId) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.APPLIED, "start review");
        loan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
        loan.setAssignedOfficerId(officerId);
        return toResponse(loans.save(loan));
    }

    @Transactional
    public LoanApplicationResponse performCreditCheck(Long id, Integer score, String remarks) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.UNDER_REVIEW, "credit check");
        
        if (score == null) {
            // Automated Credit Check Logic
            int calculatedScore = 600; // Base Score

            // Employment Bonus
            if (loan.getEmploymentType() == Loan.EmploymentType.SALARIED) {
                calculatedScore += 50;
            } else if (loan.getEmploymentType() == Loan.EmploymentType.SELF_EMPLOYED) {
                calculatedScore += 30;
            }

            // Income Bonus (10 points per 10k, max 150)
            if (loan.getMonthlyIncome() != null) {
                int incomePoints = (loan.getMonthlyIncome().intValue() / 10000) * 10;
                calculatedScore += Math.min(150, incomePoints);
            }

            // Bounds Check (300 - 900)
            score = Math.max(300, Math.min(900, calculatedScore));
        }
        
        loan.setCreditScore(score);
        loan.setRiskCategory(score >= 750 ? Loan.RiskCategory.LOW : 
                            score >= 650 ? Loan.RiskCategory.MEDIUM : Loan.RiskCategory.HIGH);
        
        if (remarks != null && !remarks.isBlank()) {
            var existing = loan.getOfficerRemarks() != null ? loan.getOfficerRemarks() + "\n" : "";
            loan.setOfficerRemarks(existing + "Credit: " + remarks);
        }
        return toResponse(loans.save(loan));
    }

    @Transactional
    public LoanApplicationResponse approveLoan(Long id, LoanApprovalRequest req) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.UNDER_REVIEW, "approve");
        
        if (loan.getCreditScore() == null) {
            throw new IllegalStateException("Cannot approve loan without performing a credit check first.");
        }
        
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setAmountApproved(req.getApprovedAmount());
        loan.setInterestRate(req.getInterestRate());
        loan.setOfficerRemarks(req.getRemarks());
        loan.setApprovedOn(LocalDateTime.now());
        
        loan = loans.save(loan);
        notify(loan, "LOAN_APPROVED", "Approved!", 
               "Loan #" + loan.getLoanId() + " approved for Rs." + loan.getAmountApproved());
        return toResponse(loan);
    }

    @Transactional
    public LoanApplicationResponse rejectLoan(Long id, String remarks) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.UNDER_REVIEW, "reject");
        
        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setOfficerRemarks(remarks);
        loan = loans.save(loan);
        notify(loan, "LOAN_REJECTED", "Update", "Loan #" + loan.getLoanId() + " rejected");
        return toResponse(loan);
    }

    @Transactional
    public LoanApplicationResponse disburseLoan(Long id) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.APPROVED, "disburse");
        
        loan.setStatus(Loan.LoanStatus.DISBURSED);
        loan = loans.save(loan);
        
        // Generate EMI Schedule
        try {
            emiClient.generateSchedule(
                    loan.getLoanId(),
                    loan.getUserId(),
                    loan.getAmountApproved(),
                    loan.getInterestRate(),
                    loan.getTenureMonths()
            );
        } catch (Exception e) {
            // Log error (should be SLF4j but using exception wrapper for now)
            throw new RuntimeException("Failed to generate EMI schedule: " + e.getMessage(), e);
        }

        notify(loan, "LOAN_DISBURSED", "Disbursed", 
               "Loan #" + loan.getLoanId() + " disbursed: Rs." + loan.getAmountApproved());
        return toResponse(loan);
    }

    private Loan findLoan(Long id) {
        return loans.findById(id).orElseThrow(() -> new LoanNotFoundException(id));
    }
    
    private void requireStatus(Loan loan, Loan.LoanStatus required, String action) {
        if (loan.getStatus() != required) {
            throw new InvalidLoanStatusException("Cannot " + action + ". Status: " + loan.getStatus());
        }
    }

    private void notify(Loan l, String type, String subject, String msg) {
        notifications.sendLoanNotification(l.getUserId(), l.getLoanId(), type, subject, msg,
                "user" + l.getUserId() + "@lms.com");
    }

    private LoanApplicationResponse toResponse(Loan l) {
        return LoanApplicationResponse.builder()
                .loanId(l.getLoanId())
                .userId(l.getUserId())
                .type(l.getType())
                .amountRequested(l.getAmountRequested())
                .tenureMonths(l.getTenureMonths())
                .purpose(l.getPurpose())
                .employmentType(l.getEmploymentType())
                .employerName(l.getEmployerName())
                .monthlyIncome(l.getMonthlyIncome())
                .annualIncome(l.getAnnualIncome())
                .existingLoans(l.getExistingLoans())
                .existingEmiAmount(l.getExistingEmiAmount())
                .interestRate(l.getInterestRate())
                .amountApproved(l.getAmountApproved())
                .status(l.getStatus())
                .officerRemarks(l.getOfficerRemarks())
                .appliedOn(l.getAppliedOn())
                .approvedOn(l.getApprovedOn())
                .build();
    }
}

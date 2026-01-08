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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private static final String NOTIFICATION_FAILED_LOG = "Failed to send notification (ignored): {}";
    private static final String LOAN_PREFIX = "Loan #";

    private final LoanRepository loans;
    private final NotificationPublisher notifications;
    private final com.lms.loan.client.EmiClient emiClient;

    @Transactional
    public LoanApplicationResponse applyLoan(LoanApplicationRequest req) {
        // Validate loan amount based on loan type
        validateLoanAmount(req.getType(), req.getAmount());
        
        var loan = Loan.builder()
                .userId(req.getUserId())
                .userEmail(req.getUserEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .type(req.getType())
                .amountRequested(req.getAmount())
                .tenureMonths(req.getTenure())
                .purpose(req.getPurpose())
                .employmentType(req.getEmploymentType())
                .employerName(req.getEmployerName())
                .monthlyIncome(req.getMonthlyIncome())
                .existingLoans(req.getExistingLoans())
                .existingEmiAmount(req.getExistingEmiAmount())
                .status(Loan.LoanStatus.APPLIED)
                .build();
        
        loan = loans.save(loan);
        try {
            notify(loan, "LOAN_APPLIED", "Application Received", 
                   "Your loan #" + loan.getLoanId() + " for Rs." + loan.getAmountRequested() + " is under review.");
        } catch (Exception e) {
            log.warn(NOTIFICATION_FAILED_LOG, e.getMessage());
        }
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

    // Officer-specific queries
    public Page<LoanApplicationResponse> getMyAssignedLoans(Long officerId, Pageable p) {
        return loans.findByAssignedOfficerId(officerId, p).map(this::toResponse);
    }

    public Page<LoanApplicationResponse> getMyLoansByStatus(Long officerId, Loan.LoanStatus status, Pageable p) {
        return loans.findByAssignedOfficerIdAndStatus(officerId, status, p).map(this::toResponse);
    }

    public Page<LoanApplicationResponse> getAvailableLoans(Pageable p) {
        return loans.findByStatusAndAssignedOfficerIdIsNull(Loan.LoanStatus.APPLIED, p).map(this::toResponse);
    }

    @Transactional
    public LoanApplicationResponse reviewLoan(Long id, Long officerId) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.APPLIED, "start review");
        
        // Ensure loan is not already assigned to another officer
        if (loan.getAssignedOfficerId() != null && !loan.getAssignedOfficerId().equals(officerId)) {
            throw new IllegalStateException("Loan is already assigned to another officer");
        }
        
        loan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
        loan.setAssignedOfficerId(officerId);
        loan.setAssignedAt(LocalDateTime.now());
        return toResponse(loans.save(loan));
    }

    @Transactional
    public LoanApplicationResponse performCreditCheck(Long id, Integer score, String remarks) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.UNDER_REVIEW, "credit check");
        
        // Credit score must be provided by officer from verified external source (credit bureau)
        // User-provided data (income, employment) should NOT be used to calculate credit score
        if (score == null || score < 300 || score > 900) {
            throw new IllegalArgumentException(
                "Credit score must be provided by verifying with external credit bureau (300-900 range)");
        }
        
        loan.setCreditScore(score);
        
        // Determine risk category based on verified credit score
        Loan.RiskCategory riskCategory;
        if (score >= 750) {
            riskCategory = Loan.RiskCategory.LOW;
        } else if (score >= 650) {
            riskCategory = Loan.RiskCategory.MEDIUM;
        } else {
            riskCategory = Loan.RiskCategory.HIGH;
        }
        loan.setRiskCategory(riskCategory);
        
        if (remarks != null && !remarks.isBlank()) {
            var existing = loan.getOfficerRemarks() != null ? loan.getOfficerRemarks() + "\n" : "";
            loan.setOfficerRemarks(existing + "Credit Check: " + remarks);
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
        
        // Ensure approved amount doesn't exceed requested amount
        if (req.getApprovedAmount().compareTo(loan.getAmountRequested()) > 0) {
            throw new IllegalArgumentException("Approved amount cannot exceed requested amount of ₹" + loan.getAmountRequested());
        }
        
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setAmountApproved(req.getApprovedAmount());
        loan.setInterestRate(req.getInterestRate());
        loan.setOfficerRemarks(req.getRemarks());
        loan.setApprovedOn(LocalDateTime.now());
        
        loan = loans.save(loan);
        try {
            notify(loan, "LOAN_APPROVED", "Approved!", 
                   LOAN_PREFIX + loan.getLoanId() + " approved for Rs." + loan.getAmountApproved());
        } catch (Exception e) {
            log.warn(NOTIFICATION_FAILED_LOG, e.getMessage());
        }
        return toResponse(loan);
    }

    @Transactional
    public LoanApplicationResponse rejectLoan(Long id, String remarks) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.UNDER_REVIEW, "reject");
        
        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setOfficerRemarks(remarks);
        loan = loans.save(loan);
        try {
            notify(loan, "LOAN_REJECTED", "Update", LOAN_PREFIX + loan.getLoanId() + " rejected");
        } catch (Exception e) {
            log.warn(NOTIFICATION_FAILED_LOG, e.getMessage());
        }
        return toResponse(loan);
    }

    @Transactional
    public LoanApplicationResponse disburseLoan(Long id) {
        var loan = findLoan(id);
        requireStatus(loan, Loan.LoanStatus.APPROVED, "disburse");
        
        loan.setStatus(Loan.LoanStatus.DISBURSED);
        loan = loans.save(loan);
        
        try {
            emiClient.generateSchedule(
                    loan.getLoanId(),
                    loan.getUserId(),
                    loan.getAmountApproved(),
                    loan.getInterestRate(),
                    loan.getTenureMonths()
            );
        } catch (Exception e) {
            throw new InvalidLoanStatusException("Failed to generate EMI schedule: " + e.getMessage());
        }

        try {
            notify(loan, "LOAN_DISBURSED", "Disbursed", 
                   LOAN_PREFIX + loan.getLoanId() + " disbursed: Rs." + loan.getAmountApproved());
        } catch (Exception e) {
            log.warn(NOTIFICATION_FAILED_LOG, e.getMessage());
        }
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
    
    private void validateLoanAmount(Loan.LoanType type, BigDecimal amount) {
        BigDecimal min;
        BigDecimal max;
        
        switch (type) {
            case HOME -> { min = new BigDecimal("500000"); max = new BigDecimal("10000000"); }
            case PERSONAL -> { min = new BigDecimal("50000"); max = new BigDecimal("1000000"); }
            case VEHICLE -> { min = new BigDecimal("100000"); max = new BigDecimal("5000000"); }
            case EDUCATION -> { min = new BigDecimal("100000"); max = new BigDecimal("3000000"); }
            case BUSINESS -> { min = new BigDecimal("200000"); max = new BigDecimal("5000000"); }
            default -> { return; }
        }
        
        if (amount.compareTo(min) < 0) {
            throw new IllegalArgumentException(
                String.format("%s loan minimum amount is ₹%s", type, min.toPlainString()));
        }
        if (amount.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                String.format("%s loan maximum amount is ₹%s", type, max.toPlainString()));
        }
    }

    private void notify(Loan l, String type, String subject, String msg) {
        String recipient = l.getUserEmail() != null ? l.getUserEmail() : "user" + l.getUserId() + "@lms.com";
        notifications.sendLoanNotification(l.getUserId(), l.getLoanId(), type, subject, msg, recipient);
    }

    private LoanApplicationResponse toResponse(Loan l) {
        String customerName = (l.getFirstName() != null ? l.getFirstName() : "") + 
                              (l.getLastName() != null ? " " + l.getLastName() : "");
        return LoanApplicationResponse.builder()
                .loanId(l.getLoanId())
                .userId(l.getUserId())
                .customerName(customerName.trim())
                .type(l.getType())
                .amountRequested(l.getAmountRequested())
                .tenureMonths(l.getTenureMonths())
                .purpose(l.getPurpose())
                .employmentType(l.getEmploymentType())
                .employerName(l.getEmployerName())
                .monthlyIncome(l.getMonthlyIncome())
                .existingLoans(l.getExistingLoans())
                .existingEmiAmount(l.getExistingEmiAmount())
                .interestRate(l.getInterestRate())
                .amountApproved(l.getAmountApproved())
                .status(l.getStatus())
                .officerRemarks(l.getOfficerRemarks())
                .appliedOn(l.getAppliedOn())
                .approvedOn(l.getApprovedOn())
                .creditScore(l.getCreditScore())
                .riskCategory(l.getRiskCategory())
                .assignedOfficerId(l.getAssignedOfficerId())
                .build();
    }

    // ============ Admin Methods ============

    @Transactional
    public LoanApplicationResponse reassignLoan(Long loanId, Long newOfficerId) {
        var loan = findLoan(loanId);
        
        if (loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW && loan.getStatus() != Loan.LoanStatus.APPLIED) {
            throw new InvalidLoanStatusException("Can only reassign loans in APPLIED or UNDER_REVIEW status");
        }
        
        loan.setAssignedOfficerId(newOfficerId);
        loan.setAssignedAt(LocalDateTime.now());
        loan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
        
        log.info("Loan {} reassigned to officer {}", loanId, newOfficerId);
        return toResponse(loans.save(loan));
    }

    @Transactional
    public LoanApplicationResponse releaseLoan(Long loanId) {
        var loan = findLoan(loanId);
        
        if (loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW) {
            throw new InvalidLoanStatusException("Can only release loans in UNDER_REVIEW status");
        }
        
        loan.setAssignedOfficerId(null);
        loan.setAssignedAt(null);
        loan.setStatus(Loan.LoanStatus.APPLIED);
        
        log.info("Loan {} released back to pool", loanId);
        return toResponse(loans.save(loan));
    }

    public long getOfficerPendingCount(Long officerId) {
        return loans.countByAssignedOfficerIdAndStatus(officerId, Loan.LoanStatus.UNDER_REVIEW);
    }

    @Transactional
    public int releaseStaleLoans(int timeoutHours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(timeoutHours);
        List<Loan> staleLoans = loans.findStaleAssignedLoans(Loan.LoanStatus.UNDER_REVIEW, cutoff);
        
        for (Loan loan : staleLoans) {
            loan.setAssignedOfficerId(null);
            loan.setAssignedAt(null);
            loan.setStatus(Loan.LoanStatus.APPLIED);
            loans.save(loan);
            log.info("Released stale loan {} (assigned > {}h ago)", loan.getLoanId(), timeoutHours);
        }
        
        return staleLoans.size();
    }
}

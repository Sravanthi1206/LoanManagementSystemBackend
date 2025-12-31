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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository repository;
    private final NotificationPublisher notificationPublisher;

    // --- Customer Features ---

    @Transactional
    public LoanApplicationResponse applyLoan(LoanApplicationRequest request) {
        Loan loan = Loan.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .amountRequested(request.getAmount())
                .tenureMonths(request.getTenure())
                .purpose(request.getPurpose())
                .employmentType(request.getEmploymentType())
                .employerName(request.getEmployerName())
                .monthlyIncome(request.getMonthlyIncome())
                .annualIncome(request.getAnnualIncome())
                .existingLoans(request.getExistingLoans())
                .existingEmiAmount(request.getExistingEmiAmount())
                .status(Loan.LoanStatus.APPLIED)
                .build();
        
        Loan savedLoan = repository.save(loan);
        
        // Send notification via RabbitMQ (async)
        notificationPublisher.sendLoanNotification(
                savedLoan.getUserId(),
                savedLoan.getLoanId(),
                "LOAN_APPLIED",
                "Loan Application Received",
                String.format("Your loan application #%d for ₹%.2f has been received and is under review.", 
                        savedLoan.getLoanId(), savedLoan.getAmountRequested()),
                "user" + savedLoan.getUserId() + "@lms.com"
        );
        
        return mapToResponse(savedLoan);
    }

    public List<LoanApplicationResponse> getMyLoans(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LoanApplicationResponse getLoanById(Long id) {
        Loan loan = repository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
        return mapToResponse(loan);
    }
    
    public Loan getLoan(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
    }

    @Transactional
    public LoanApplicationResponse withdrawLoan(Long loanId, Long userId) {
        Loan loan = getLoan(loanId);
        
        if (!loan.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only withdraw your own loan applications");
        }
        
        if (loan.getStatus() != Loan.LoanStatus.APPLIED) {
            throw new InvalidLoanStatusException("Cannot withdraw loan. Current status: " + loan.getStatus());
        }
        
        loan.setStatus(Loan.LoanStatus.WITHDRAWN);
        Loan savedLoan = repository.save(loan);
        return mapToResponse(savedLoan);
    }

    // --- Officer Features ---

    public Page<LoanApplicationResponse> getLoansByStatus(Loan.LoanStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(this::mapToResponse);
    }
    
    public Page<LoanApplicationResponse> getAllLoans(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public LoanApplicationResponse reviewLoan(Long id, Long officerId) {
        Loan loan = getLoan(id);
        
        if (loan.getStatus() != Loan.LoanStatus.APPLIED) {
            throw new InvalidLoanStatusException("Loan must be in APPLIED status to start review. Current: " + loan.getStatus());
        }
        
        loan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
        loan.setAssignedOfficerId(officerId);
        Loan savedLoan = repository.save(loan);
        return mapToResponse(savedLoan);
    }

    @Transactional
    public LoanApplicationResponse performCreditCheck(Long id, Integer creditScore, String remarks) {
        Loan loan = getLoan(id);
        
        if (loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW) {
            throw new InvalidLoanStatusException("Loan must be UNDER_REVIEW for credit check. Current: " + loan.getStatus());
        }
        
        loan.setCreditScore(creditScore);
        
        if (creditScore >= 750) {
            loan.setRiskCategory(Loan.RiskCategory.LOW);
        } else if (creditScore >= 650) {
            loan.setRiskCategory(Loan.RiskCategory.MEDIUM);
        } else {
            loan.setRiskCategory(Loan.RiskCategory.HIGH);
        }
        
        if (remarks != null && !remarks.isEmpty()) {
            String existingRemarks = loan.getOfficerRemarks() != null ? loan.getOfficerRemarks() + "\n" : "";
            loan.setOfficerRemarks(existingRemarks + "Credit Check: " + remarks);
        }
        
        Loan savedLoan = repository.save(loan);
        return mapToResponse(savedLoan);
    }

    @Transactional
    public LoanApplicationResponse approveLoan(Long id, LoanApprovalRequest request) {
        Loan loan = getLoan(id);
        
        if (loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW) {
            throw new InvalidLoanStatusException("Loan must be UNDER_REVIEW to approve. Current: " + loan.getStatus());
        }
        
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setAmountApproved(request.getApprovedAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setOfficerRemarks(request.getRemarks());
        loan.setApprovedOn(LocalDateTime.now());
        
        Loan savedLoan = repository.save(loan);
        
        // Send approval notification via RabbitMQ (async)
        notificationPublisher.sendLoanNotification(
                savedLoan.getUserId(),
                savedLoan.getLoanId(),
                "LOAN_APPROVED",
                "Loan Application Approved! 🎉",
                String.format("Congratulations! Your loan #%d has been approved for ₹%.2f at %.2f%% interest rate.", 
                        savedLoan.getLoanId(), savedLoan.getAmountApproved(), savedLoan.getInterestRate()),
                "user" + savedLoan.getUserId() + "@lms.com"
        );
        
        return mapToResponse(savedLoan);
    }

    @Transactional
    public LoanApplicationResponse rejectLoan(Long id, String remarks) {
        Loan loan = getLoan(id);
        
        if (loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW) {
            throw new InvalidLoanStatusException("Loan must be UNDER_REVIEW to reject. Current: " + loan.getStatus());
        }
        
        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setOfficerRemarks(remarks);
        
        Loan savedLoan = repository.save(loan);
        
        // Send rejection notification via RabbitMQ (async)
        notificationPublisher.sendLoanNotification(
                savedLoan.getUserId(),
                savedLoan.getLoanId(),
                "LOAN_REJECTED",
                "Loan Application Update",
                String.format("Your loan application #%d has been reviewed. Status: Rejected. Remarks: %s", 
                        savedLoan.getLoanId(), remarks != null ? remarks : "N/A"),
                "user" + savedLoan.getUserId() + "@lms.com"
        );
        
        return mapToResponse(savedLoan);
    }

    private LoanApplicationResponse mapToResponse(Loan loan) {
        return LoanApplicationResponse.builder()
                .loanId(loan.getLoanId())
                .userId(loan.getUserId())
                .type(loan.getType())
                .amountRequested(loan.getAmountRequested())
                .tenureMonths(loan.getTenureMonths())
                .purpose(loan.getPurpose())
                .employmentType(loan.getEmploymentType())
                .employerName(loan.getEmployerName())
                .monthlyIncome(loan.getMonthlyIncome())
                .annualIncome(loan.getAnnualIncome())
                .existingLoans(loan.getExistingLoans())
                .existingEmiAmount(loan.getExistingEmiAmount())
                .interestRate(loan.getInterestRate())
                .amountApproved(loan.getAmountApproved())
                .status(loan.getStatus())
                .officerRemarks(loan.getOfficerRemarks())
                .appliedOn(loan.getAppliedOn())
                .approvedOn(loan.getApprovedOn())
                .build();
    }
}


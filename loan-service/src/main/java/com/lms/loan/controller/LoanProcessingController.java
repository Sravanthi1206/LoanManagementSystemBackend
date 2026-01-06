package com.lms.loan.controller;

import com.lms.loan.dto.CreditCheckRequest;
import com.lms.loan.dto.LoanApplicationResponse;
import com.lms.loan.dto.LoanApprovalRequest;
import com.lms.loan.dto.RejectRequest;
import com.lms.loan.entity.Loan;
import com.lms.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans/admin")
@RequiredArgsConstructor
public class LoanProcessingController {

    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<Page<LoanApplicationResponse>> getAllLoans(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PageableDefault(size = 10, sort = "appliedOn") Pageable pageable) {
        
        if ("LOAN_OFFICER".equals(role)) {
            return ResponseEntity.ok(loanService.getMyAssignedLoans(userId, pageable));
        }
        return ResponseEntity.ok(loanService.getAllLoans(pageable));
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<LoanApplicationResponse>> getPendingLoans(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PageableDefault(size = 10, sort = "appliedOn") Pageable pageable) {
        
        if ("LOAN_OFFICER".equals(role)) {
            return ResponseEntity.ok(loanService.getMyLoansByStatus(userId, Loan.LoanStatus.APPLIED, pageable));
        }
        return ResponseEntity.ok(loanService.getLoansByStatus(Loan.LoanStatus.APPLIED, pageable));
    }

    @GetMapping("/under-review")
    public ResponseEntity<Page<LoanApplicationResponse>> getUnderReviewLoans(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PageableDefault(size = 10, sort = "appliedOn") Pageable pageable) {
        
        if ("LOAN_OFFICER".equals(role)) {
            return ResponseEntity.ok(loanService.getMyLoansByStatus(userId, Loan.LoanStatus.UNDER_REVIEW, pageable));
        }
        return ResponseEntity.ok(loanService.getLoansByStatus(Loan.LoanStatus.UNDER_REVIEW, pageable));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<Page<LoanApplicationResponse>> getLoansByStatus(
            @PathVariable Loan.LoanStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        if ("LOAN_OFFICER".equals(role)) {
            return ResponseEntity.ok(loanService.getMyLoansByStatus(userId, status, pageable));
        }
        return ResponseEntity.ok(loanService.getLoansByStatus(status, pageable));
    }

    // Officer-specific endpoints
    @GetMapping("/my-loans")
    public ResponseEntity<Page<LoanApplicationResponse>> getMyLoans(
            @RequestHeader("X-User-Id") Long officerId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(loanService.getMyAssignedLoans(officerId, pageable));
    }

    @GetMapping("/my-loans/{status}")
    public ResponseEntity<Page<LoanApplicationResponse>> getMyLoansByStatus(
            @RequestHeader("X-User-Id") Long officerId,
            @PathVariable Loan.LoanStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(loanService.getMyLoansByStatus(officerId, status, pageable));
    }

    @GetMapping("/available")
    public ResponseEntity<Page<LoanApplicationResponse>> getAvailableLoans(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(loanService.getAvailableLoans(pageable));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<LoanApplicationResponse> startReview(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long officerId) {
        return ResponseEntity.ok(loanService.reviewLoan(id, officerId));
    }

    @PostMapping("/{id}/credit-check")
    public ResponseEntity<LoanApplicationResponse> performCreditCheck(
            @PathVariable Long id,
            @Valid @RequestBody CreditCheckRequest request) {
        return ResponseEntity.ok(loanService.performCreditCheck(id, request.getCreditScore(), request.getRemarks()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LoanApplicationResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody LoanApprovalRequest request) {
        return ResponseEntity.ok(loanService.approveLoan(id, request));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LoanApplicationResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(loanService.rejectLoan(id, request.getRemarks()));
    }

    @PutMapping("/{id}/disburse")
    public ResponseEntity<LoanApplicationResponse> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.disburseLoan(id));
    }

    // ============ Admin-specific endpoints ============

    @PutMapping("/{id}/reassign/{officerId}")
    public ResponseEntity<LoanApplicationResponse> reassignLoan(
            @PathVariable Long id,
            @PathVariable Long officerId) {
        return ResponseEntity.ok(loanService.reassignLoan(id, officerId));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<LoanApplicationResponse> releaseLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.releaseLoan(id));
    }

    @GetMapping("/officer/{officerId}/pending-count")
    public ResponseEntity<Long> getOfficerPendingCount(@PathVariable Long officerId) {
        return ResponseEntity.ok(loanService.getOfficerPendingCount(officerId));
    }
}


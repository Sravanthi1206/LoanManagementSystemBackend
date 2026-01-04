package com.lms.loan.controller;

import com.lms.loan.dto.CreateLoanResponse;
import com.lms.loan.dto.LoanApplicationRequest;
import com.lms.loan.dto.LoanApplicationResponse;
import com.lms.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<CreateLoanResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        // Set userId from header if not in request
        if (request.getUserId() == null && userId != null) {
            request.setUserId(userId);
        }
        
        LoanApplicationResponse response = loanService.applyLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateLoanResponse.builder().loanId(response.getLoanId()).build());
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanApplicationResponse>> getMyLoans(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long effectiveUserId = userId != null ? userId : headerUserId;
        if (effectiveUserId == null) {
            throw new IllegalArgumentException("userId is required as query param or X-User-Id header");
        }
        return ResponseEntity.ok(loanService.getMyLoans(effectiveUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<LoanApplicationResponse> withdraw(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(loanService.withdrawLoan(id, userId));
    }
}

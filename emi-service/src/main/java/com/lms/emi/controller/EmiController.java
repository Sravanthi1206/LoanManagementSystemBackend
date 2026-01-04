package com.lms.emi.controller;

import com.lms.emi.dto.EmiCalculationRequest;
import com.lms.emi.dto.EmiCalculationResponse;
import com.lms.emi.entity.RepaymentSchedule;
import com.lms.emi.service.EmiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/emi")
@RequiredArgsConstructor
public class EmiController {

    private final EmiService emiService;

    /**
     * Calculate EMI preview without creating a schedule
     */
    @PostMapping("/calculate")
    public ResponseEntity<EmiCalculationResponse> calculateEmi(
            @Valid @RequestBody EmiCalculationRequest request) {
        EmiCalculationResponse response = emiService.calculateEmi(
                request.getPrincipalAmount(),
                request.getAnnualInterestRate(),
                request.getTenureMonths()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Internal endpoint for LoanService to call when loan is approved
     */
    @PostMapping("/generate")
    public ResponseEntity<Void> generateSchedule(
            @RequestParam("loanId") Long loanId,
            @RequestParam("userId") Long userId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("rate") BigDecimal rate,
            @RequestParam("tenure") Integer tenure) {
        emiService.generateSchedule(loanId, userId, amount, rate, tenure);
        return ResponseEntity.ok().build();
    }

    /**
     * Get EMI schedule for a loan
     */
    @GetMapping("/schedule/{loanId}")
    public ResponseEntity<List<RepaymentSchedule>> getSchedule(@PathVariable("loanId") Long loanId) {
        return ResponseEntity.ok(emiService.getSchedule(loanId));
    }

    /**
     * Get upcoming EMIs for a user
     */
    @GetMapping("/upcoming/{userId}")
    public ResponseEntity<List<RepaymentSchedule>> getUpcomingEmis(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(emiService.getUpcomingEmis(userId));
    }

    /**
     * Mark an installment as paid
     */
    @PutMapping("/installment/{id}/paid")
    public ResponseEntity<RepaymentSchedule> markAsPaid(@PathVariable("id") Long id) {
        return ResponseEntity.ok(emiService.markInstallmentAsPaid(id));
    }
}

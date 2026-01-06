package com.lms.loan.controller;

import com.lms.loan.dto.TransactionResponse;
import com.lms.loan.dto.WalletResponse;
import com.lms.loan.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for virtual wallet operations.
 */
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Get wallet balance for a user.
     * In production, this would get userId from JWT token.
     */
    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getBalance(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long effectiveUserId = userId != null ? userId : headerUserId;
        if (effectiveUserId == null) {
            throw new IllegalArgumentException("userId is required as query param or X-User-Id header");
        }
        return ResponseEntity.ok(walletService.getBalance(effectiveUserId));
    }

    /**
     * Get transaction history for a user.
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId, pageable));
    }

    /**
     * Get balance by user ID (for internal service calls).
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<WalletResponse> getBalanceByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    /**
     * Debit amount from wallet (for internal service calls).
     */
    @PostMapping("/debit")
    public ResponseEntity<Void> debitWallet(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") java.math.BigDecimal amount) {
        walletService.debit(userId, amount, com.lms.loan.entity.WalletTransaction.TransactionType.EMI_PAYMENT, null, "Wallet Payment");
        return ResponseEntity.ok().build();
    }

    /**
     * Credit amount to wallet (for payment gateway top-ups).
     */
    @PostMapping("/credit")
    public ResponseEntity<TransactionResponse> creditWallet(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") java.math.BigDecimal amount,
            @RequestParam(value = "description", defaultValue = "Wallet Top-up") String description) {
        TransactionResponse response = walletService.credit(userId, amount, 
            com.lms.loan.entity.WalletTransaction.TransactionType.TOP_UP, null, description);
        return ResponseEntity.ok(response);
    }
}

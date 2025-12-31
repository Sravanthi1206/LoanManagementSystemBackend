package com.lms.loan.service;

import com.lms.loan.dto.TransactionResponse;
import com.lms.loan.dto.WalletResponse;
import com.lms.loan.entity.UserWallet;
import com.lms.loan.entity.WalletTransaction;
import com.lms.loan.entity.WalletTransaction.TransactionType;
import com.lms.loan.exception.InsufficientBalanceException;
import com.lms.loan.repository.WalletRepository;
import com.lms.loan.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for virtual wallet operations (demo transactions).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal("100000.00");

    /**
     * Get wallet balance for a user. Creates wallet if not exists.
     */
    public WalletResponse getBalance(Long userId) {
        UserWallet wallet = getOrCreateWallet(userId);
        return WalletResponse.builder()
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .lastUpdated(wallet.getLastUpdated())
                .build();
    }

    /**
     * Credit amount to wallet (e.g., loan disbursement).
     */
    @Transactional
    public TransactionResponse credit(Long userId, BigDecimal amount, 
                                       TransactionType type, Long loanId, String description) {
        UserWallet wallet = getOrCreateWallet(userId);
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                userId, loanId, type, amount, newBalance, description);
        
        log.info("Credited ₹{} to user {} wallet. New balance: ₹{}", amount, userId, newBalance);
        return mapToResponse(transaction);
    }

    /**
     * Debit amount from wallet (e.g., EMI payment).
     */
    @Transactional
    public TransactionResponse debit(Long userId, BigDecimal amount,
                                      TransactionType type, Long loanId, String description) {
        UserWallet wallet = getOrCreateWallet(userId);
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(wallet.getBalance(), amount);
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = createTransaction(
                userId, loanId, type, amount.negate(), newBalance, description);
        
        log.info("Debited ₹{} from user {} wallet. New balance: ₹{}", amount, userId, newBalance);
        return mapToResponse(transaction);
    }

    /**
     * Disburse loan amount to user's wallet.
     */
    @Transactional
    public TransactionResponse disburseLoan(Long userId, Long loanId, 
                                            BigDecimal amount, String applicationNumber) {
        String description = String.format("Loan Disbursement - %s", applicationNumber);
        return credit(userId, amount, TransactionType.DISBURSEMENT, loanId, description);
    }

    /**
     * Process EMI payment from wallet.
     */
    @Transactional
    public TransactionResponse payEmi(Long userId, Long loanId, 
                                       BigDecimal amount, int installmentNo) {
        String description = String.format("EMI Payment - Installment %d", installmentNo);
        return debit(userId, amount, TransactionType.EMI_PAYMENT, loanId, description);
    }

    /**
     * Get transaction history for user.
     */
    public Page<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    // --- Helper Methods ---

    private UserWallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserWallet newWallet = UserWallet.builder()
                            .userId(userId)
                            .balance(DEFAULT_BALANCE)
                            .build();
                    log.info("Created new wallet for user {} with balance ₹{}", userId, DEFAULT_BALANCE);
                    return walletRepository.save(newWallet);
                });
    }

    private WalletTransaction createTransaction(Long userId, Long loanId,
                                                  TransactionType type, BigDecimal amount,
                                                  BigDecimal balanceAfter, String description) {
        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .userId(userId)
                .loanId(loanId)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();
        return transactionRepository.save(transaction);
    }

    private TransactionResponse mapToResponse(WalletTransaction tx) {
        return TransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalanceAfter())
                .description(tx.getDescription())
                .loanId(tx.getLoanId())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}

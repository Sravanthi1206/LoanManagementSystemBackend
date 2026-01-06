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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class WalletService {

    private final WalletRepository wallets;
    private final WalletTransactionRepository transactions;
    private final WalletService self;

    public WalletService(WalletRepository wallets, WalletTransactionRepository transactions, @org.springframework.context.annotation.Lazy WalletService self) {
        this.wallets = wallets;
        this.transactions = transactions;
        this.self = self;
    }

    public WalletResponse getBalance(Long userId) {
        var wallet = findOrCreate(userId);
        return new WalletResponse(wallet.getUserId(), wallet.getBalance(), wallet.getLastUpdated());
    }

    @Transactional
    public TransactionResponse credit(Long userId, BigDecimal amount, TransactionType type, Long loanId, String desc) {
        var wallet = findOrCreate(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallets.save(wallet);
        
        var txn = recordTransaction(userId, loanId, type, amount, wallet.getBalance(), desc);
        log.info("Credited {} to user {}", amount, userId);
        return toResponse(txn);
    }

    @Transactional
    public TransactionResponse debit(Long userId, BigDecimal amount, TransactionType type, Long loanId, String desc) {
        var wallet = findOrCreate(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(wallet.getBalance(), amount);
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallets.save(wallet);
        
        var txn = recordTransaction(userId, loanId, type, amount.negate(), wallet.getBalance(), desc);
        log.info("Debited {} from user {}", amount, userId);
        return toResponse(txn);
    }

    @Transactional
    public TransactionResponse disburseLoan(Long userId, Long loanId, BigDecimal amount, String appNo) {
        return self.credit(userId, amount, TransactionType.DISBURSEMENT, loanId, "Loan Disbursement - " + appNo);
    }

    @Transactional
    public TransactionResponse payEmi(Long userId, Long loanId, BigDecimal amount, int installmentNo) {
        return self.debit(userId, amount, TransactionType.EMI_PAYMENT, loanId, "EMI #" + installmentNo);
    }

    public Page<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable) {
        return transactions.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    private UserWallet findOrCreate(Long userId) {
        return wallets.findByUserId(userId).orElseGet(() -> {
            var w = new UserWallet();
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            log.info("New wallet created for user {}", userId);
            return wallets.save(w);
        });
    }

    private WalletTransaction recordTransaction(Long userId, Long loanId, TransactionType type, 
                                                 BigDecimal amount, BigDecimal balance, String desc) {
        var txn = new WalletTransaction();
        txn.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        txn.setUserId(userId);
        txn.setLoanId(loanId);
        txn.setType(type);
        txn.setAmount(amount);
        txn.setBalanceAfter(balance);
        txn.setDescription(desc);
        return transactions.save(txn);
    }

    private TransactionResponse toResponse(WalletTransaction t) {
        var r = new TransactionResponse();
        r.setTransactionId(t.getTransactionId());
        r.setType(t.getType());
        r.setAmount(t.getAmount());
        r.setBalanceAfter(t.getBalanceAfter());
        r.setDescription(t.getDescription());
        r.setLoanId(t.getLoanId());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}

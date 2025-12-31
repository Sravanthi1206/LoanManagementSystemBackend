package com.lms.loan.repository;

import com.lms.loan.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<WalletTransaction> findByLoanIdOrderByCreatedAtDesc(Long loanId);

    List<WalletTransaction> findByUserIdAndTypeOrderByCreatedAtDesc(
            Long userId, WalletTransaction.TransactionType type);
}

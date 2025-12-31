package com.lms.loan.repository;

import com.lms.loan.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<UserWallet, Long> {

    Optional<UserWallet> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserWallet w SET w.balance = w.balance + :amount WHERE w.userId = :userId")
    int creditBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE UserWallet w SET w.balance = w.balance - :amount WHERE w.userId = :userId AND w.balance >= :amount")
    int debitBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}

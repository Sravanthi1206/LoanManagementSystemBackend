package com.lms.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Virtual Wallet entity.
 * Each customer has a wallet with a balance for simulating transactions.
 */
@Entity
@Table(name = "user_wallet")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWallet {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = new BigDecimal("100000.00"); // ₹1,00,000 starting balance

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

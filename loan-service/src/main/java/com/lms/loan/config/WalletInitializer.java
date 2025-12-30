package com.lms.loan.config;

import com.lms.loan.entity.UserWallet;
import com.lms.loan.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initializes demo wallets for test users.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletInitializer implements CommandLineRunner {

    private final WalletRepository walletRepository;

    @Override
    public void run(String... args) {
        log.info("Checking for demo wallets...");
        
        // Create wallet for customer (userId = 3, based on order of creation)
        for (long userId = 1; userId <= 3; userId++) {
            if (walletRepository.findByUserId(userId).isEmpty()) {
                UserWallet wallet = new UserWallet();
                wallet.setUserId(userId);
                wallet.setBalance(new BigDecimal("100000.00")); // ₹1,00,000 demo balance
                walletRepository.save(wallet);
                log.info("✅ Created demo wallet for user {} with ₹1,00,000 balance", userId);
            }
        }

        log.info("===== DEMO WALLET INFO =====");
        log.info("All users have ₹1,00,000 demo balance");
        log.info("=============================");
    }
}

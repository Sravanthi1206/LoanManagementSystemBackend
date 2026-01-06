package com.lms.loan.config;

import com.lms.loan.entity.UserWallet;
import com.lms.loan.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initializes wallets for users.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletInitializer implements CommandLineRunner {

    private final WalletRepository walletRepository;

    @Override
    public void run(String... args) {
        log.info("Wallet initializer started - wallets will be created on-demand with zero balance");
        // Wallets are now created on-demand when users access them
        // No pre-seeding with fake balance
    }
}

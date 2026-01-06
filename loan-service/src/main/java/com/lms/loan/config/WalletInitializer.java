package com.lms.loan.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes wallets for users.
 * Wallets are now created on-demand when users access them.
 */
@Component
@Slf4j
public class WalletInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Wallet initializer started - wallets will be created on-demand with zero balance");
    }
}

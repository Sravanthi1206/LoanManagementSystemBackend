package com.lms.loan.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Wallet Initializer Tests")
class WalletInitializerTest {

    @Test
    @DisplayName("Run - should complete without errors")
    void runShouldCompleteWithoutErrors() {
        WalletInitializer walletInitializer = new WalletInitializer();
        assertDoesNotThrow(() -> walletInitializer.run());
    }
}

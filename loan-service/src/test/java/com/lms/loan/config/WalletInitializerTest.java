package com.lms.loan.config;

import com.lms.loan.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Initializer Tests")
class WalletInitializerTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletInitializer walletInitializer;

    @Test
    @DisplayName("Run - should not create wallets (on-demand creation)")
    void run_NoPreCreation() throws Exception {
        // WalletInitializer no longer pre-creates wallets
        // Wallets are created on-demand with zero balance
        walletInitializer.run();
        
        // Should never save any wallets on startup
        verify(walletRepository, never()).save(any());
        verify(walletRepository, never()).findByUserId(anyLong());
    }
}

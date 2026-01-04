package com.lms.loan.config;

import com.lms.loan.entity.UserWallet;
import com.lms.loan.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Initializer Tests")
class WalletInitializerTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletInitializer walletInitializer;

    @Test
    @DisplayName("Run - wallet creation when missing")
    void run_WalletCreation() throws Exception {
        // Mock wallet not found
        when(walletRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(walletRepository.save(any(UserWallet.class))).thenAnswer(i -> i.getArguments()[0]);

        walletInitializer.run();

        verify(walletRepository, atLeastOnce()).save(argThat(w -> 
            w instanceof UserWallet && 
            ((UserWallet) w).getBalance().compareTo(new BigDecimal("100000.00")) == 0
        ));
    }
    
    @Test
    @DisplayName("Run - wallet exists")
    void run_WalletExists() throws Exception {
        when(walletRepository.findByUserId(anyLong())).thenReturn(Optional.of(new UserWallet()));
        
        walletInitializer.run();
        
        // Should not save if exists (logic loop checks each ID)
        // Since we mock all findByUserId calls to return something, it should skip save.
        verify(walletRepository, never()).save(any(UserWallet.class));
    }
}

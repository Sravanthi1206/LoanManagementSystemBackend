package com.lms.loan.service;

import com.lms.loan.dto.TransactionResponse;
import com.lms.loan.dto.WalletResponse;
import com.lms.loan.entity.UserWallet;
import com.lms.loan.entity.WalletTransaction;
import com.lms.loan.entity.WalletTransaction.TransactionType;
import com.lms.loan.exception.InsufficientBalanceException;
import com.lms.loan.repository.WalletRepository;
import com.lms.loan.repository.WalletTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Service Tests")
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    @DisplayName("Get Balance - existing wallet")
    void getBalance_ExistingWallet() {
        UserWallet wallet = UserWallet.builder()
                .userId(1L)
                .balance(new BigDecimal("5000.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getBalance(1L);

        assertEquals(1L, response.getUserId());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
        verify(walletRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Get Balance - create new wallet")
    void getBalance_NewWallet() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(UserWallet.class))).thenAnswer(i -> i.getArguments()[0]);

        WalletResponse response = walletService.getBalance(1L);

        assertEquals(1L, response.getUserId());
        // Default balance logic in service
        assertEquals(new BigDecimal("100000"), response.getBalance());
        verify(walletRepository).save(any(UserWallet.class));
    }

    @Test
    @DisplayName("Credit - success")
    void credit_Success() {
        UserWallet wallet = UserWallet.builder().userId(1L).balance(new BigDecimal("1000.00")).build();
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(UserWallet.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransactionResponse response = walletService.credit(1L, new BigDecimal("500.00"), TransactionType.DISBURSEMENT, 101L, "Test Credit");

        assertEquals(new BigDecimal("1500.00"), response.getBalanceAfter()); // 1000 + 500
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(TransactionType.DISBURSEMENT, response.getType());
        verify(walletRepository).save(wallet);
    }

    @Test
    @DisplayName("Debit - success")
    void debit_Success() {
        UserWallet wallet = UserWallet.builder().userId(1L).balance(new BigDecimal("1000.00")).build();
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(UserWallet.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransactionResponse response = walletService.debit(1L, new BigDecimal("400.00"), TransactionType.EMI_PAYMENT, 101L, "Test Debit");

        assertEquals(new BigDecimal("600.00"), response.getBalanceAfter()); // 1000 - 400
        // Debit amount in response is negative? Service logic: amount.negate() for transaction.
        // Let's check service logic if I need to assert negative. Service saves negative, maps from tx.
        assertEquals(new BigDecimal("-400.00"), response.getAmount());
        assertEquals(TransactionType.EMI_PAYMENT, response.getType());
    }

    @Test
    @DisplayName("Debit - insufficient balance")
    void debit_InsufficientBalance() {
        UserWallet wallet = UserWallet.builder().userId(1L).balance(new BigDecimal("100.00")).build();
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class, () -> 
            walletService.debit(1L, new BigDecimal("500.00"), TransactionType.EMI_PAYMENT, 101L, "Test Debit")
        );
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Disburse Loan - calls credit")
    void disburseLoan() {
        // Mocking credit flow implicitly by mocking deps
        UserWallet wallet = UserWallet.builder().userId(1L).balance(BigDecimal.ZERO).build();
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(UserWallet.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransactionResponse response = walletService.disburseLoan(1L, 101L, new BigDecimal("10000.00"), "APP-001");

        assertEquals(new BigDecimal("10000.00"), response.getBalanceAfter());
        assertEquals(TransactionType.DISBURSEMENT, response.getType());
    }

    @Test
    @DisplayName("Pay EMI - calls debit")
    void payEmi() {
         UserWallet wallet = UserWallet.builder().userId(1L).balance(new BigDecimal("5000.00")).build();
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(UserWallet.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransactionResponse response = walletService.payEmi(1L, 101L, new BigDecimal("2000.00"), 1);

        assertEquals(new BigDecimal("3000.00"), response.getBalanceAfter());
        assertEquals(TransactionType.EMI_PAYMENT, response.getType());
    }

    @Test
    @DisplayName("Get Transaction History")
    void getTransactionHistory() {
        Pageable pageable = PageRequest.of(0, 10);
        WalletTransaction tx = WalletTransaction.builder().transactionId("TX1").build();
        Page<WalletTransaction> page = new PageImpl<>(Collections.singletonList(tx));

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);

        Page<TransactionResponse> result = walletService.getTransactionHistory(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("TX1", result.getContent().get(0).getTransactionId());
    }
}

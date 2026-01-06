package com.lms.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletTopupRequest {
    private Long userId;
    private BigDecimal amount;
    private String currency = "INR";
    private String paymentMethod; // CARD, UPI, NET_BANKING
    
    // Card details (for CARD method)
    private String cardNumber;
    private String cardExpiry;
    private String cvv;
    private String cardHolderName;
    
    // UPI details (for UPI method)
    private String upiId;
}

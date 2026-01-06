package com.lms.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GatewayPaymentRequest {
    private Long loanId;
    private Long userId;
    private BigDecimal amount;
    private String currency = "INR";
    private String description;
    private String cardNumber;
    private String cardExpiry;
    private String cvv;
    private String cardHolderName;
}

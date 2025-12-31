package com.lms.payment.dto;

import com.lms.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long loanId;
    private Long userId;
    private Payment.PaymentType paymentType;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String transactionId;
    private String referenceNumber;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
}

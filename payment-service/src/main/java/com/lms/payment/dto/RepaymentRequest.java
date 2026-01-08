package com.lms.payment.dto;

import com.lms.payment.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepaymentRequest {
    
    @NotNull(message = "Loan ID is required")
    private Long loanId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Long installmentId; // If paying specific installment
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.0", message = "Minimum payment is ₹100")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;
    
    private String referenceNumber;
    
    private String userEmail; // For sending payment confirmation notification
}

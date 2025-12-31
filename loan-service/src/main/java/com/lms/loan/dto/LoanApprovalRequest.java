package com.lms.loan.dto;

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
public class LoanApprovalRequest {
    
    @NotNull(message = "Approved amount is required")
    @DecimalMin(value = "10000.0", message = "Minimum approved amount is ₹10,000")
    @DecimalMax(value = "10000000.0", message = "Maximum approved amount is ₹1,00,00,000")
    private BigDecimal approvedAmount;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "6.0", message = "Minimum interest rate is 6%")
    @DecimalMax(value = "25.0", message = "Maximum interest rate is 25%")
    private BigDecimal interestRate;
    
    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}

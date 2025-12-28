package com.lms.emi.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmiCalculationRequest {
    
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "10000.0", message = "Minimum loan amount is ₹10,000")
    @JsonAlias({"principal", "amount"})
    private BigDecimal principalAmount;
    
    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "6.0", message = "Minimum interest rate is 6%")
    @DecimalMax(value = "25.0", message = "Maximum interest rate is 25%")
    private BigDecimal annualInterestRate;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Minimum tenure is 6 months")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenureMonths;
}

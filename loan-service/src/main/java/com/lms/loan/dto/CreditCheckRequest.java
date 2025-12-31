package com.lms.loan.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditCheckRequest {
    
    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score cannot exceed 900")
    private Integer creditScore;
    
    private String riskCategory; // LOW, MEDIUM, HIGH (auto-calculated if not provided)
    
    private String remarks;
}

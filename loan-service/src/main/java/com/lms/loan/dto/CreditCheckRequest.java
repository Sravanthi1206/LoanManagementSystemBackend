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
    
    private Integer creditScore;
    
    private String riskCategory; // LOW, MEDIUM, HIGH (auto-calculated if not provided)
    
    private String remarks;
}

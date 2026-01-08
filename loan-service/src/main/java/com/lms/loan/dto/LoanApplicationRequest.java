package com.lms.loan.dto;

import com.lms.loan.entity.Loan;
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
public class LoanApplicationRequest {
    
    private Long userId;
    
    private String userEmail;
    
    private String firstName;
    
    private String lastName;
    
    @NotNull(message = "Loan type is required")
    private Loan.LoanType type;
    
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "10000.0", message = "Minimum loan amount is ₹10,000")
    @DecimalMax(value = "10000000.0", message = "Maximum loan amount is ₹1,00,00,000")
    private BigDecimal amount;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Minimum tenure is 6 months")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenure;
    
    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 500, message = "Purpose must be between 10-500 characters")
    private String purpose;
    
    @NotNull(message = "Employment type is required")
    private Loan.EmploymentType employmentType;
    
    @Size(max = 200, message = "Employer name cannot exceed 200 characters")
    private String employerName;
    
    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "10000.0", message = "Minimum monthly income is ₹10,000")
    private BigDecimal monthlyIncome;
    
    @Builder.Default
    private Boolean existingLoans = false;
    
    @DecimalMin(value = "0.0", message = "Existing EMI amount cannot be negative")
    @Builder.Default
    private BigDecimal existingEmiAmount = BigDecimal.ZERO;
}

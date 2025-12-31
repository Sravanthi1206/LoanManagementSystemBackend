package com.lms.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectRequest {
    
    @NotBlank(message = "Rejection remarks are required")
    @Size(min = 10, max = 500, message = "Remarks must be between 10-500 characters")
    private String remarks;
}

package com.lms.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for loan creation endpoints.
 * Returns only the ID of the created loan.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateLoanResponse {
    private Long loanId;
}

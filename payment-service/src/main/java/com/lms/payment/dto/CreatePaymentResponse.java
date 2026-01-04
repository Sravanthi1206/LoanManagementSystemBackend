package com.lms.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for payment creation endpoints.
 * Returns only the ID and transaction ID of the created payment.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentResponse {
    private Long id;
    private String transactionId;
}

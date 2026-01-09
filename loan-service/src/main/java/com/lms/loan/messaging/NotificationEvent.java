package com.lms.loan.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message event for notifications sent via RabbitMQ.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private Long loanId;
    private String eventType; // LOAN_APPLIED, LOAN_APPROVED, LOAN_REJECTED, EMI_REMINDER, PAYMENT_RECEIVED
    private String subject;
    private String message;
    private String recipient;
    private String loanType;
    private LocalDateTime timestamp;
}

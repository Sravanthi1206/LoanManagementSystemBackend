package com.lms.identity.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Notification event for RabbitMQ messaging.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Long userId;
    private Long loanId;
    private String eventType;
    private String subject;
    private String message;
    private String recipient;
    private LocalDateTime timestamp;
    
    // Credential email fields (used for STAFF_ACCOUNT_CREATED events)
    private String firstName;
    private String temporaryPassword;
    private String role;
}

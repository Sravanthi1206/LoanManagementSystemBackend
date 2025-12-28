package com.lms.emi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending notifications via notification-service.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    
    private Long userId;
    private Long loanId;
    private NotificationType type;
    private String subject;
    private String message;
    private String recipient;
    
    public enum NotificationType {
        EMAIL, SMS, BOTH
    }
}

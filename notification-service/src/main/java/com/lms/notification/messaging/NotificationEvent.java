package com.lms.notification.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message event received from RabbitMQ for notifications.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private Long loanId;
    private String eventType;
    private String subject;
    private String message;
    private String recipient;
    private LocalDateTime timestamp;
}

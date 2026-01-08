package com.lms.payment.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private Long userId;
    private Long loanId;
    private String eventType;
    private String subject;
    private String message;
    private String recipient;
    private LocalDateTime timestamp;
}

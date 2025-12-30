package com.lms.notification.dto;

import com.lms.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private String id;  // MongoDB uses String IDs
    private Long userId;
    private Long loanId;
    private Notification.NotificationType type;
    private String subject;
    private String message;
    private Notification.NotificationStatus status;
    private String recipient;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}

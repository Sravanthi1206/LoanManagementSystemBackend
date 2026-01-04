package com.lms.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;  // MongoDB uses String IDs by default

    @Indexed
    private Long userId;

    @Indexed
    private Long loanId;

    private NotificationType type;

    private String subject;

    private String message;

    private NotificationStatus status;

    private String recipient; // Email or phone number

    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public enum NotificationType {
        EMAIL, SMS, BOTH
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED, READ
    }
}

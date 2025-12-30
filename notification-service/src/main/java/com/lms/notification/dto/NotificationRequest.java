package com.lms.notification.dto;

import com.lms.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Long loanId;
    
    @NotNull(message = "Notification type is required")
    private Notification.NotificationType type;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String message;
    
    @NotBlank(message = "Recipient is required")
    private String recipient;
}

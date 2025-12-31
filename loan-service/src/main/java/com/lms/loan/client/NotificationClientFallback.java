package com.lms.loan.client;

import com.lms.loan.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation when notification-service is unavailable.
 * Logs the notification request but doesn't fail the main operation.
 */
@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {
    
    @Override
    public ResponseEntity<String> sendNotification(NotificationRequest request) {
        log.warn("Notification service is unavailable. Failed to send notification to user {} for loan {}", 
                request.getUserId(), request.getLoanId());
        log.info("Notification details - Subject: {}, Message: {}", 
                request.getSubject(), request.getMessage());
        return ResponseEntity.ok("Notification queued (service unavailable)");
    }
}

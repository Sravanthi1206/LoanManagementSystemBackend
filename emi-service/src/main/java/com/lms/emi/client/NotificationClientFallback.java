package com.lms.emi.client;

import com.lms.emi.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation when notification-service is unavailable.
 */
@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {
    
    @Override
    public ResponseEntity<String> sendNotification(NotificationRequest request) {
        log.warn("Notification service unavailable. Failed to send EMI notification for loan {}", 
                request.getLoanId());
        return ResponseEntity.ok("Notification queued (service unavailable)");
    }
}

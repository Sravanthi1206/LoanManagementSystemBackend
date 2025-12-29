package com.lms.emi.client;

import com.lms.emi.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for communication with Notification Service.
 * Used to send EMI reminders and payment notifications.
 */
@FeignClient(name = "notification-service", fallback = NotificationClientFallback.class)
public interface NotificationClient {
    
    @PostMapping("/notifications/send")
    ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request);
}

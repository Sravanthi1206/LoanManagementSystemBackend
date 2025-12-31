package com.lms.loan.client;

import com.lms.loan.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for communication with Notification Service.
 * Falls back gracefully if notification service is unavailable.
 */
@FeignClient(name = "notification-service", fallback = NotificationClientFallback.class)
public interface NotificationClient {
    
    @PostMapping("/notifications/send")
    ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request);
}

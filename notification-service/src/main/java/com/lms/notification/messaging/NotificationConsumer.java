package com.lms.notification.messaging;

import com.lms.notification.config.RabbitMQConfig;
import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.entity.Notification;
import com.lms.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer that listens to notification events from RabbitMQ queue.
 * Processes incoming messages and creates notifications via NotificationService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: {} for loan {} to user {}", 
                event.getEventType(), event.getLoanId(), event.getUserId());
        
        try {
            // Convert event to notification request and process
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .loanId(event.getLoanId())
                    .type(Notification.NotificationType.EMAIL)
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .recipient(event.getRecipient())
                    .eventType(event.getEventType())
                    .loanType(event.getLoanType())
                    .firstName(event.getFirstName())
                    .temporaryPassword(event.getTemporaryPassword())
                    .role(event.getRole())
                    .build();
            
            notificationService.sendNotification(request);
            log.info("Successfully processed notification for event type {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Failed to process notification event for loan {}: {}", 
                    event.getLoanId(), e.getMessage());
            // In production, you might want to send to a dead-letter queue
        }
    }
}

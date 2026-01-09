package com.lms.identity.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Publisher service for sending notification events to RabbitMQ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Publish a notification event to the message queue.
     */
    public void publishNotification(NotificationEvent event) {
        try {
            event.setTimestamp(LocalDateTime.now());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );
            log.info("Published notification event: {} to user {}", 
                    event.getEventType(), event.getRecipient());
        } catch (Exception e) {
            log.error("Failed to publish notification event: {}", e.getMessage());
            // Don't throw - notification failures should not break the main flow
        }
    }
    
    /**
     * Send account credentials notification to new staff member.
     */
    public void sendCredentialsNotification(String email, String firstName, String password, String role) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(0L)
                .loanId(0L)
                .eventType("STAFF_ACCOUNT_CREATED")
                .subject("Your LMS Account Created")
                .message("Account created with temporary password")
                .recipient(email)
                .firstName(firstName)
                .temporaryPassword(password)
                .role(role)
                .build();
        publishNotification(event);
    }
    
    /**
     * Send account activated notification.
     */
    public void sendAccountActivatedNotification(String email, String firstName, String role) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(0L)
                .loanId(0L)
                .eventType("ACCOUNT_ACTIVATED")
                .subject("Your LMS Account Has Been Activated")
                .message("Your account has been activated")
                .recipient(email)
                .firstName(firstName)
                .role(role)
                .build();
        publishNotification(event);
    }
    
    /**
     * Send account deactivated notification.
     */
    public void sendAccountDeactivatedNotification(String email, String firstName, String role) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(0L)
                .loanId(0L)
                .eventType("ACCOUNT_DEACTIVATED")
                .subject("Your LMS Account Has Been Deactivated")
                .message("Your account has been deactivated")
                .recipient(email)
                .firstName(firstName)
                .role(role)
                .build();
        publishNotification(event);
    }
}

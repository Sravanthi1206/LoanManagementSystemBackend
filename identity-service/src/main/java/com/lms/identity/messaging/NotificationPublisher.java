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
                .message(buildCredentialsMessage(firstName, email, password, role))
                .recipient(email)
                .build();
        publishNotification(event);
    }
    
    private String buildCredentialsMessage(String firstName, String email, String password, String role) {
        return String.format(
            "Dear %s,\\n\\n" +
            "Your %s account has been created in the Loan Management System.\\n\\n" +
            "Login Credentials:\\n" +
            "Email: %s\\n" +
            "Password: %s\\n\\n" +
            "IMPORTANT: Please change your password after first login.\\n\\n" +
            "Best regards,\\n" +
            "LMS Admin Team",
            firstName, role.replace("_", " "), email, password
        );
    }
}

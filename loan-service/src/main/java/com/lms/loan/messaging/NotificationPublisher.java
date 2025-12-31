package com.lms.loan.messaging;

import com.lms.loan.config.RabbitMQConfig;
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
            log.info("Published notification event: {} for loan {} to user {}", 
                    event.getEventType(), event.getLoanId(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish notification event: {}", e.getMessage());
            // Don't throw - notification failures should not break the main flow
        }
    }
    
    /**
     * Convenience method to create and publish a loan notification.
     */
    public void sendLoanNotification(Long userId, Long loanId, String eventType, 
                                      String subject, String message, String recipient) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(userId)
                .loanId(loanId)
                .eventType(eventType)
                .subject(subject)
                .message(message)
                .recipient(recipient)
                .build();
        publishNotification(event);
    }
}

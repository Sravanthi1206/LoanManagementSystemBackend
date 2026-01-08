package com.lms.payment.messaging;

import com.lms.payment.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
            log.info("Published payment notification: {} for loan {} to user {}", 
                    event.getEventType(), event.getLoanId(), event.getUserId());
        } catch (Exception e) {
            log.warn("Failed to publish payment notification: {}", e.getMessage());
        }
    }
    
    /**
     * Convenience method to send a payment notification.
     */
    public void sendPaymentNotification(Long userId, Long loanId, String eventType, 
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

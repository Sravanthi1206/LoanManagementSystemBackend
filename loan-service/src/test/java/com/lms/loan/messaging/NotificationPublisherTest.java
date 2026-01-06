package com.lms.loan.messaging;

import com.lms.loan.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class NotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublisher notificationPublisher;

    @Test
    public void testPublishNotificationSuccess() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L)
                .loanId(100L)
                .eventType("TEST_EVENT")
                .subject("Test Subject")
                .message("Test Message")
                .recipient("test@example.com")
                .build();

        notificationPublisher.publishNotification(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                eq(event)
        );
    }

    @Test
    public void testPublishNotificationException() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L)
                .loanId(100L)
                .build();
        
        doThrow(new AmqpException("Connection failed")).when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(NotificationEvent.class));

        // Should not throw exception
        notificationPublisher.publishNotification(event);
        
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                eq(event)
        );
    }

    @Test
    public void testSendLoanNotification() {
        notificationPublisher.sendLoanNotification(
                1L, 100L, "TEST_EVENT", "Subject", "Message", "test@example.com"
        );

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                any(NotificationEvent.class)
        );
    }
}

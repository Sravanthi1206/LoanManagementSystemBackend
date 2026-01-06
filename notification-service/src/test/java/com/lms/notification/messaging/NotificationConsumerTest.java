package com.lms.notification.messaging;

import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.entity.Notification;
import com.lms.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    public void testHandleNotificationEventSuccess() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L)
                .loanId(100L)
                .eventType("TEST")
                .subject("Subject")
                .message("Message")
                .recipient("test@example.com")
                .timestamp(LocalDateTime.now())
                .build();

        notificationConsumer.handleNotificationEvent(event);

        verify(notificationService).sendNotification(any(NotificationRequest.class));
    }

    @Test
    public void testHandleNotificationEventException() {
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L)
                .loanId(100L)
                .build();

        doThrow(new RuntimeException("Processing failed")).when(notificationService)
                .sendNotification(any(NotificationRequest.class));

        // Should not throw exception
        notificationConsumer.handleNotificationEvent(event);

        verify(notificationService).sendNotification(any(NotificationRequest.class));
    }
}

package com.lms.loan.messaging;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationEventTest {

    @Test
    public void testNotificationEventBuilderAndAccessors() {
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L)
                .loanId(100L)
                .eventType("TEST")
                .subject("Subject")
                .message("Message")
                .recipient("test@example.com")
                .timestamp(now)
                .build();

        assertEquals(1L, event.getUserId());
        assertEquals(100L, event.getLoanId());
        assertEquals("TEST", event.getEventType());
        assertEquals("Subject", event.getSubject());
        assertEquals("Message", event.getMessage());
        assertEquals("test@example.com", event.getRecipient());
        assertEquals(now, event.getTimestamp());
    }

    @Test
    public void testNoArgsConstructor() {
        NotificationEvent event = new NotificationEvent();
        assertNull(event.getUserId());
    }

    @Test
    public void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent event = new NotificationEvent(
                1L, 100L, "TEST", "Subject", "Message", "test@example.com", now
        );
        assertEquals(1L, event.getUserId());
    }
}

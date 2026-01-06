package com.lms.loan.messaging;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationEventTest {

    private static final String TEST_EMAIL = "test@example.com";

    private static final String TEST_SUBJECT = "Subject";
    private static final String TEST_MESSAGE = "Message";

    @Test
    public void testNotificationEventBuilderAndAccessors() {
        LocalDateTime now = LocalDateTime.now();
        NotificationEvent event = NotificationEvent.builder()
                .userId(1L)
                .loanId(100L)
                .eventType("TEST")
                .subject(TEST_SUBJECT)
                .message(TEST_MESSAGE)
                .recipient(TEST_EMAIL)
                .timestamp(now)
                .build();

        assertEquals(1L, event.getUserId());
        assertEquals(100L, event.getLoanId());
        assertEquals("TEST", event.getEventType());
        assertEquals(TEST_SUBJECT, event.getSubject());
        assertEquals(TEST_MESSAGE, event.getMessage());
        assertEquals(TEST_EMAIL, event.getRecipient());
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
                1L, 100L, "TEST", TEST_SUBJECT, TEST_MESSAGE, TEST_EMAIL, now
        );
        assertEquals(1L, event.getUserId());
    }
}

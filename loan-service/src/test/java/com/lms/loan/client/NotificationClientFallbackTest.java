package com.lms.loan.client;

import com.lms.loan.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NotificationClientFallbackTest {

    @Test
    public void testSendNotificationFallback() {
        NotificationClientFallback fallback = new NotificationClientFallback();
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .loanId(100L)
                .subject("Test")
                .message("Message")
                .recipient("test@example.com")
                .build();

        ResponseEntity<String> response = fallback.sendNotification(request);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notification queued (service unavailable)", response.getBody());
    }
}

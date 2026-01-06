package com.lms.notification.exception;

/**
 * Exception thrown when a notification is not found.
 */
public class NotificationNotFoundException extends RuntimeException {
    
    public NotificationNotFoundException(String notificationId) {
        super("Notification not found with id: " + notificationId);
    }
}

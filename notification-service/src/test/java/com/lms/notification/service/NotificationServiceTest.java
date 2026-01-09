package com.lms.notification.service;

import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.dto.NotificationResponse;
import com.lms.notification.entity.Notification;
import com.lms.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest request;
    private Notification notification;

    @BeforeEach
    void setUp() {
        request = NotificationRequest.builder()
                .userId(1L)
                .loanId(101L)
                .type(Notification.NotificationType.EMAIL)
                .subject("Subject")
                .message("Message")
                .recipient("test@example.com")
                .build();

        notification = Notification.builder()
                .id("notif-1")
                .userId(1L)
                .loanId(101L)
                .type(Notification.NotificationType.EMAIL)
                .subject("Subject")
                .message("Message")
                .recipient("test@example.com")
                .status(Notification.NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Send notification - Success")
    void sendNotification_Success() {
        when(repository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(emailService).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());

        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals("SENT", response.getStatus().toString());
        verify(emailService, times(1)).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
        verify(repository, times(2)).save(any(Notification.class)); // Initial save + status update
    }

    @Test
    @DisplayName("Get user notifications - Success")
    void getUserNotifications_Success() {
        Page<Notification> page = new PageImpl<>(Collections.singletonList(notification));
        when(repository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(page);

        Page<NotificationResponse> result = notificationService.getUserNotifications(1L, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Get unread notifications - Success")
    void getUnreadNotifications_Success() {
        when(repository.findByUserIdAndStatus(anyLong(), eq(Notification.NotificationStatus.SENT))) // Service queries SENT as unread logic?
                .thenReturn(Collections.singletonList(notification));

        // Note: Service code queries 'SENT' status in getUnreadNotifications
        List<NotificationResponse> result = notificationService.getUnreadNotifications(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Mark as read - Success")
    void markAsRead_Success() {
        when(repository.findById("notif-1")).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead("notif-1");

        assertNotNull(response);
        assertEquals(Notification.NotificationStatus.READ, notification.getStatus());
    }
    @Test
    @DisplayName("Send notification - Failure logged (trySend catch block)")
    void sendNotification_EmailFailure() {
        // Mock email failure
        doThrow(new RuntimeException("Mail server down")).when(emailService)
                .sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
        
        // Initial save returns pending notification
        Notification pending = notification.toBuilder().status(Notification.NotificationStatus.PENDING).build();
        // Second save (after failure) returns failed notification
        Notification failed = notification.toBuilder().status(Notification.NotificationStatus.FAILED).build();
        
        when(repository.save(any(Notification.class)))
                .thenReturn(pending)
                .thenReturn(failed);

        // Should not throw exception, but logged
        NotificationResponse response = notificationService.sendNotification(request);

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus().toString());
        verify(emailService).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Get notification by ID - Success")
    void getNotificationById_Success() {
        when(repository.findById("notif-1")).thenReturn(Optional.of(notification));
        
        NotificationResponse response = notificationService.getNotificationById("notif-1");
        
        assertNotNull(response);
        assertEquals("notif-1", response.getId());
    }
    
    @Test
    @DisplayName("Get notification by ID - Not Found")
    void getNotificationById_NotFound() {
        when(repository.findById("invalid")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> notificationService.getNotificationById("invalid"));
    }

    @Test
    @DisplayName("Send Loan Status Notification")
    void sendLoanStatusNotification_Success() {
        when(repository.save(any(Notification.class))).thenReturn(notification);
        
        assertDoesNotThrow(() -> 
            notificationService.sendLoanStatusNotification(1L, 101L, "APPROVED", "test@example.com")
        );
        
        verify(emailService, times(1)).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Send EMI Reminder Notification")
    void sendEmiReminderNotification_Success() {
        when(repository.save(any(Notification.class))).thenReturn(notification);
        
        assertDoesNotThrow(() -> 
            notificationService.sendEmiReminderNotification(1L, 101L, "2026-01-01", "5000", "test@example.com")
        );
        
        verify(emailService, times(1)).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Send notification with BOTH type - Email sent")
    void sendNotification_BothType_SendsEmail() {
        // Setup request with BOTH type
        NotificationRequest bothRequest = NotificationRequest.builder()
                .userId(1L)
                .loanId(101L)
                .type(Notification.NotificationType.BOTH)
                .subject("Subject")
                .message("Message")
                .recipient("test@example.com")
                .build();
        
        Notification bothNotification = notification.toBuilder()
                .type(Notification.NotificationType.BOTH)
                .build();
        
        when(repository.save(any(Notification.class))).thenReturn(bothNotification);
        doNothing().when(emailService).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());

        NotificationResponse response = notificationService.sendNotification(bothRequest);

        assertNotNull(response);
        // Verify email was sent for BOTH type
        verify(emailService, times(1)).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Send notification with SMS type - Email NOT sent")
    void sendNotification_SmsType_NoEmailSent() {
        // Setup request with SMS type (not EMAIL or BOTH)
        NotificationRequest smsRequest = NotificationRequest.builder()
                .userId(1L)
                .loanId(101L)
                .type(Notification.NotificationType.SMS)
                .subject("Subject")
                .message("Message")
                .recipient("1234567890")
                .build();
        
        Notification smsNotification = notification.toBuilder()
                .type(Notification.NotificationType.SMS)
                .build();
        
        when(repository.save(any(Notification.class))).thenReturn(smsNotification);

        NotificationResponse response = notificationService.sendNotification(smsRequest);

        assertNotNull(response);
        // Verify email was NOT sent for SMS type
        verify(emailService, never()).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Send notification with STAFF_ACCOUNT_CREATED event type - Credentials email sent")
    void sendNotification_StaffAccountCreated_CredentialsEmailSent() {
        // Setup request with STAFF_ACCOUNT_CREATED event type
        NotificationRequest credentialsRequest = NotificationRequest.builder()
                .userId(0L)
                .loanId(0L)
                .type(Notification.NotificationType.EMAIL)
                .subject("Your LMS Account Created")
                .message("Account created with temporary password")
                .recipient("newstaff@example.com")
                .eventType("STAFF_ACCOUNT_CREATED")
                .firstName("John")
                .temporaryPassword("TempPass123")
                .role("LOAN_OFFICER")
                .build();
        
        Notification credentialsNotification = notification.toBuilder()
                .recipient("newstaff@example.com")
                .subject("Your LMS Account Created")
                .build();
        
        when(repository.save(any(Notification.class))).thenReturn(credentialsNotification);
        doNothing().when(emailService).sendAccountCredentialsEmail(anyString(), anyString(), anyString(), anyString(), anyString());

        NotificationResponse response = notificationService.sendNotification(credentialsRequest);

        assertNotNull(response);
        // Verify credentials email was sent, not loan status email
        verify(emailService, times(1)).sendAccountCredentialsEmail(
                eq("newstaff@example.com"),
                eq("John"),
                eq("LOAN_OFFICER"),
                eq("newstaff@example.com"),
                eq("TempPass123")
        );
        verify(emailService, never()).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Send notification with ACCOUNT_ACTIVATED event type - Activation email sent")
    void sendNotification_AccountActivated_ActivationEmailSent() {
        NotificationRequest activationRequest = NotificationRequest.builder()
                .userId(0L)
                .loanId(0L)
                .type(Notification.NotificationType.EMAIL)
                .subject("Your LMS Account Has Been Activated")
                .message("Your account has been activated")
                .recipient("staff@example.com")
                .eventType("ACCOUNT_ACTIVATED")
                .firstName("Jane")
                .role("LOAN_OFFICER")
                .build();
        
        Notification activationNotification = notification.toBuilder()
                .recipient("staff@example.com")
                .build();
        
        when(repository.save(any(Notification.class))).thenReturn(activationNotification);
        doNothing().when(emailService).sendAccountActivatedEmail(anyString(), anyString(), anyString());

        NotificationResponse response = notificationService.sendNotification(activationRequest);

        assertNotNull(response);
        verify(emailService, times(1)).sendAccountActivatedEmail(
                eq("staff@example.com"),
                eq("Jane"),
                eq("LOAN_OFFICER")
        );
        verify(emailService, never()).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Send notification with ACCOUNT_DEACTIVATED event type - Deactivation email sent")
    void sendNotification_AccountDeactivated_DeactivationEmailSent() {
        NotificationRequest deactivationRequest = NotificationRequest.builder()
                .userId(0L)
                .loanId(0L)
                .type(Notification.NotificationType.EMAIL)
                .subject("Your LMS Account Has Been Deactivated")
                .message("Your account has been deactivated")
                .recipient("staff@example.com")
                .eventType("ACCOUNT_DEACTIVATED")
                .firstName("Jane")
                .role("LOAN_OFFICER")
                .build();
        
        Notification deactivationNotification = notification.toBuilder()
                .recipient("staff@example.com")
                .build();
        
        when(repository.save(any(Notification.class))).thenReturn(deactivationNotification);
        doNothing().when(emailService).sendAccountDeactivatedEmail(anyString(), anyString(), anyString());

        NotificationResponse response = notificationService.sendNotification(deactivationRequest);

        assertNotNull(response);
        verify(emailService, times(1)).sendAccountDeactivatedEmail(
                eq("staff@example.com"),
                eq("Jane"),
                eq("LOAN_OFFICER")
        );
        verify(emailService, never()).sendLoanStatusEmail(anyString(), anyString(), anyString(), anyString());
    }
}


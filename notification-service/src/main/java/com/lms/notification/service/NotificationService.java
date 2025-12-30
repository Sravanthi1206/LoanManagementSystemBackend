package com.lms.notification.service;

import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.dto.NotificationResponse;
import com.lms.notification.entity.Notification;
import com.lms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailService emailService;

    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .loanId(request.getLoanId())
                .type(request.getType())
                .subject(request.getSubject())
                .message(request.getMessage())
                .recipient(request.getRecipient())
                .status(Notification.NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = repository.save(notification);
        
        // Simulate sending notification
        boolean success = simulateSendNotification(saved);
        
        if (success) {
            saved.setStatus(Notification.NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
        } else {
            saved.setStatus(Notification.NotificationStatus.FAILED);
        }
        
        saved = repository.save(saved);
        return mapToResponse(saved);
    }

    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return repository.findByUserIdAndStatus(userId, Notification.NotificationStatus.SENT)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse markAsRead(String id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        notification.setStatus(Notification.NotificationStatus.READ);
        Notification saved = repository.save(notification);
        return mapToResponse(saved);
    }

    public NotificationResponse getNotificationById(String id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        return mapToResponse(notification);
    }

    // Predefined notification templates
    public void sendLoanStatusNotification(Long userId, Long loanId, String status, String recipient) {
        String subject = "Loan Application Status Update";
        String message = String.format("Your loan application #%d status has been updated to: %s", loanId, status);
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .loanId(loanId)
                .type(Notification.NotificationType.EMAIL)
                .subject(subject)
                .message(message)
                .recipient(recipient)
                .build();
        
        sendNotification(request);
    }

    public void sendEmiReminderNotification(Long userId, Long loanId, String dueDate, String amount, String recipient) {
        String subject = "EMI Payment Reminder";
        String message = String.format("Reminder: Your EMI of ₹%s for loan #%d is due on %s. Please ensure timely payment.", 
                amount, loanId, dueDate);
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .loanId(loanId)
                .type(Notification.NotificationType.BOTH)
                .subject(subject)
                .message(message)
                .recipient(recipient)
                .build();
        
        sendNotification(request);
    }

    private boolean simulateSendNotification(Notification notification) {
        log.info("Sending {} notification to {}: Subject - {}", 
                notification.getType(), 
                notification.getRecipient(), 
                notification.getSubject());
        
        try {
            // Send actual email using EmailService
            if (notification.getType() == Notification.NotificationType.EMAIL ||
                notification.getType() == Notification.NotificationType.BOTH) {
                emailService.sendSimpleEmail(
                    notification.getRecipient(),
                    notification.getSubject(),
                    notification.getMessage()
                );
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            return false;
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .loanId(notification.getLoanId())
                .type(notification.getType())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .recipient(notification.getRecipient())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

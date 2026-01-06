package com.lms.notification.service;

import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.dto.NotificationResponse;
import com.lms.notification.entity.Notification;
import com.lms.notification.entity.Notification.NotificationStatus;
import com.lms.notification.entity.Notification.NotificationType;
import com.lms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repo;
    private final EmailService emails;

    public NotificationResponse sendNotification(NotificationRequest req) {
        var n = Notification.builder()
                .userId(req.getUserId())
                .loanId(req.getLoanId())
                .type(req.getType())
                .subject(req.getSubject())
                .message(req.getMessage())
                .recipient(req.getRecipient())
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        n = repo.save(n);
        
        boolean sent = trySend(n);
        n.setStatus(sent ? NotificationStatus.SENT : NotificationStatus.FAILED);
        if (sent) n.setSentAt(LocalDateTime.now());
        
        return toResponse(repo.save(n));
    }

    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable p) {
        return repo.findByUserId(userId, p).map(this::toResponse);
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return repo.findByUserIdAndStatus(userId, NotificationStatus.SENT)
                .stream().map(this::toResponse).toList();
    }

    public NotificationResponse markAsRead(String id) {
        var n = find(id);
        n.setStatus(NotificationStatus.READ);
        return toResponse(repo.save(n));
    }

    public NotificationResponse getNotificationById(String id) {
        return toResponse(find(id));
    }

    public void sendLoanStatusNotification(Long userId, Long loanId, String status, String recipient) {
        var req = NotificationRequest.builder()
                .userId(userId)
                .loanId(loanId)
                .type(NotificationType.EMAIL)
                .subject("Loan Status Update")
                .message("Loan #" + loanId + " status: " + status)
                .recipient(recipient)
                .build();
        sendNotification(req);
    }

    public void sendEmiReminderNotification(Long userId, Long loanId, String dueDate, String amount, String recipient) {
        var req = NotificationRequest.builder()
                .userId(userId)
                .loanId(loanId)
                .type(NotificationType.BOTH)
                .subject("EMI Reminder")
                .message("EMI Rs." + amount + " for loan #" + loanId + " due " + dueDate)
                .recipient(recipient)
                .build();
        sendNotification(req);
    }

    private Notification find(String id) {
        return repo.findById(id).orElseThrow(() -> new com.lms.notification.exception.NotificationNotFoundException(id));
    }

    private boolean trySend(Notification n) {
        log.info("Sending to {}: {}", n.getRecipient(), n.getSubject());
        try {
            if (n.getType() == NotificationType.EMAIL || n.getType() == NotificationType.BOTH) {
                // Send styled HTML email for better presentation
                String loanNumber = n.getLoanId() != null ? "#" + n.getLoanId() : "N/A";
                emails.sendLoanStatusEmail(n.getRecipient(), loanNumber, n.getSubject(), n.getMessage());
            }
            return true;
        } catch (Exception e) {
            log.error("Send failed: {}", e.getMessage());
            return false;
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .loanId(n.getLoanId())
                .type(n.getType())
                .subject(n.getSubject())
                .message(n.getMessage())
                .status(n.getStatus())
                .recipient(n.getRecipient())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

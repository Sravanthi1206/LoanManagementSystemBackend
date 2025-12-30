package com.lms.notification.repository;

import com.lms.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(Long userId);
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    List<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    List<Notification> findByLoanId(Long loanId);
    List<Notification> findByStatus(Notification.NotificationStatus status);
}

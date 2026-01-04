package com.lms.emi.repository;

import com.lms.emi.entity.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanId(Long loanId);
    // Find upcoming due payments for notification
    List<RepaymentSchedule> findByDueDateBetween(java.time.LocalDate start, java.time.LocalDate end);
    List<RepaymentSchedule> findByStatusAndDueDateBetween(RepaymentSchedule.PaymentStatus status, java.time.LocalDate start, java.time.LocalDate end);
    List<RepaymentSchedule> findByUserIdAndStatusAndDueDateBetween(Long userId, RepaymentSchedule.PaymentStatus status, java.time.LocalDate start, java.time.LocalDate end);
}

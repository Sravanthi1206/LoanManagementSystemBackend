package com.lms.payment.repository;

import com.lms.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByLoanId(Long loanId);
    List<Payment> findByUserId(Long userId);
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByLoanIdAndPaymentType(Long loanId, Payment.PaymentType paymentType);
}

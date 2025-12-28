package com.lms.emi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "repayment_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanId; // Logical FK

    private Integer installmentNo; // 1 to N
    private LocalDate dueDate;

    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalEmi;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDate paidDate;

    public enum PaymentStatus {
        PENDING, PAID, OVERDUE
    }
}

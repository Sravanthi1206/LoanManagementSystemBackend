package com.lms.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @Column(nullable = false)
    private Long userId; // Logical FK to Identity Service
    
    @Column(length = 255)
    private String userEmail; // User's email for notifications

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountRequested;

    @Column(nullable = false)
    private Integer tenureMonths;
    
    @Column(length = 500)
    private String purpose;
    
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;
    
    @Column(length = 200)
    private String employerName;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyIncome;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal annualIncome;
    
    @Builder.Default
    private Boolean existingLoans = false;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal existingEmiAmount = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate; // Assigned by Officer
    
    @Column(precision = 15, scale = 2)
    private BigDecimal amountApproved; // Can be different from requested

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Lob
    private String officerRemarks;
    
    private Long assignedOfficerId;
    
    private LocalDateTime assignedAt;
    
    private Integer creditScore;
    
    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory;


    @Column(updatable = false)
    private LocalDateTime appliedOn;
    
    @Column
    private LocalDateTime approvedOn;
    
    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedOn = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = LoanStatus.APPLIED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum LoanType {
        PERSONAL, HOME, VEHICLE, EDUCATION, BUSINESS
    }

    public enum LoanStatus {
        APPLIED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED, CLOSED, WITHDRAWN
    }
    
    public enum EmploymentType {
        SALARIED, SELF_EMPLOYED, BUSINESS
    }
    
    public enum RiskCategory {
        LOW, MEDIUM, HIGH
    }
}

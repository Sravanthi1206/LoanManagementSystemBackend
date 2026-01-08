package com.lms.identity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String phone;
    
    private LocalDate dateOfBirth;
    
    @Column(unique = true)
    private String panCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean passwordChangeRequired = false;
    
    // Approval workflow fields - backward compatible with defaults
    @Builder.Default
    private Boolean approved = true;  // Default true for existing users
    
    @Builder.Default
    private Boolean approvalPending = false;
    
    private Long createdByUserId;  // Who created this user (null for self-registered)

    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    // Credit score for loan eligibility assessment
    @Builder.Default
    private Integer creditScore = 650;  // Default starting score for new users
    
    private LocalDateTime creditScoreUpdatedAt;
    
    // Financial profile for loan applications
    private java.math.BigDecimal monthlyIncome;
    private String employmentType;  // SALARIED, SELF_EMPLOYED, BUSINESS

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (passwordChangeRequired == null) {
            passwordChangeRequired = false;
        }
        if (approved == null) {
            approved = true;
        }
        if (approvalPending == null) {
            approvalPending = false;
        }
        if (creditScore == null) {
            creditScore = 650;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Role {
        ROOT_ADMIN, ADMIN, LOAN_OFFICER, CUSTOMER
    }
}


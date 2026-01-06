package com.lms.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String filePath;

    private Long fileSize;

    private String contentType;

    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public enum DocumentType {
        IDENTITY_PROOF, ADDRESS_PROOF, INCOME_PROOF, BANK_STATEMENT, PROPERTY_DOCUMENTS
    }
}

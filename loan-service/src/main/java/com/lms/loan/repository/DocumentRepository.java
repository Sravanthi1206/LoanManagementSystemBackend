package com.lms.loan.repository;

import com.lms.loan.entity.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<ApplicationDocument, Long> {
    List<ApplicationDocument> findByApplicationId(Long applicationId);
    List<ApplicationDocument> findByApplicationIdAndDocumentType(Long applicationId, ApplicationDocument.DocumentType documentType);
}

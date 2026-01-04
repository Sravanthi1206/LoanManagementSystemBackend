package com.lms.loan.service;

import com.lms.loan.dto.DocumentResponse;
import com.lms.loan.entity.ApplicationDocument;
import com.lms.loan.entity.Loan;
import com.lms.loan.exception.InvalidLoanStatusException;
import com.lms.loan.exception.LoanNotFoundException;
import com.lms.loan.repository.DocumentRepository;
import com.lms.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final LoanRepository loanRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Transactional
    public DocumentResponse uploadDocument(Long applicationId, ApplicationDocument.DocumentType documentType,
                                           MultipartFile file) throws IOException {
        Loan loan = loanRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));
        
        if (loan.getStatus() != Loan.LoanStatus.APPLIED &&
            loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW) {
            throw new InvalidLoanStatusException("Cannot upload documents. Loan status: " + loan.getStatus());
        }

        Path uploadPath = Paths.get(uploadDir, "loan_" + applicationId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        ApplicationDocument document = ApplicationDocument.builder()
                .applicationId(applicationId)
                .documentType(documentType)
                .documentName(originalFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        return mapToResponse(documentRepository.save(document));
    }

    public List<DocumentResponse> getDocuments(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DocumentResponse getDocument(Long documentId) {
        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        return mapToResponse(document);
    }

    @Transactional
    public void deleteDocument(Long documentId) throws IOException {
        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        Path filePath = Paths.get(document.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        documentRepository.delete(document);
    }

    private DocumentResponse mapToResponse(ApplicationDocument document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .applicationId(document.getApplicationId())
                .documentType(document.getDocumentType())
                .documentName(document.getDocumentName())
                .filePath(document.getFilePath())
                .fileSize(document.getFileSize())
                .contentType(document.getContentType())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}

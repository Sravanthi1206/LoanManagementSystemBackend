package com.lms.loan.controller;

import com.lms.loan.dto.DocumentResponse;
import com.lms.loan.entity.ApplicationDocument;
import com.lms.loan.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/loans/{applicationId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long applicationId,
            @RequestParam("documentType") ApplicationDocument.DocumentType documentType,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        DocumentResponse response = documentService.uploadDocument(applicationId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable Long applicationId) {
        return ResponseEntity.ok(documentService.getDocuments(applicationId));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId) throws IOException {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}

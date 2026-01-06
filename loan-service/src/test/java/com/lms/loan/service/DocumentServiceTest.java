package com.lms.loan.service;

import com.lms.loan.dto.DocumentResponse;
import com.lms.loan.entity.ApplicationDocument;
import com.lms.loan.entity.Loan;
import com.lms.loan.exception.InvalidLoanStatusException;
import com.lms.loan.exception.LoanNotFoundException;
import com.lms.loan.repository.DocumentRepository;
import com.lms.loan.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Document Service Tests")
class DocumentServiceTest {

    private static final String TEST_PDF = "test.pdf";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String CONTENT = "content";

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    @DisplayName("Upload Document - success")
    void uploadDocumentSuccess(@TempDir Path tempDir) throws IOException {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        Loan loan = Loan.builder().loanId(1L).status(Loan.LoanStatus.APPLIED).build();
        MockMultipartFile file = new MockMultipartFile("file", TEST_PDF, APPLICATION_PDF, CONTENT.getBytes());

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(i -> {
            ApplicationDocument doc = i.getArgument(0);
            doc.setId(101L);
            return doc;
        });

        DocumentResponse response = documentService.uploadDocument(1L, ApplicationDocument.DocumentType.IDENTITY_PROOF, file);

        assertNotNull(response);
        assertEquals(TEST_PDF, response.getDocumentName());
        assertTrue(Files.exists(tempDir.resolve("loan_1"))); // Folder created
        verify(documentRepository).save(any(ApplicationDocument.class));
    }

    @Test
    @DisplayName("Upload Document - invalid loan status")
    void uploadDocumentInvalidStatus(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
        Loan loan = Loan.builder().loanId(1L).status(Loan.LoanStatus.APPROVED).build();
        MockMultipartFile file = new MockMultipartFile("file", TEST_PDF, APPLICATION_PDF, CONTENT.getBytes());

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(InvalidLoanStatusException.class, () -> 
            documentService.uploadDocument(1L, ApplicationDocument.DocumentType.IDENTITY_PROOF, file)
        );
        verify(documentRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Upload Document - loan not found")
    void uploadDocumentLoanNotFound() {
        MockMultipartFile file = new MockMultipartFile("file", TEST_PDF, APPLICATION_PDF, CONTENT.getBytes());
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(LoanNotFoundException.class, () -> 
             documentService.uploadDocument(1L, ApplicationDocument.DocumentType.IDENTITY_PROOF, file)
        );
    }

    @Test
    @DisplayName("Get Documents")
    void getDocuments() {
        ApplicationDocument doc = ApplicationDocument.builder().id(101L).documentName(TEST_PDF).build();
        when(documentRepository.findByApplicationId(1L)).thenReturn(Collections.singletonList(doc));

        List<DocumentResponse> responses = documentService.getDocuments(1L);

        assertEquals(1, responses.size());
        assertEquals(101L, responses.get(0).getId());
    }

    @Test
    @DisplayName("Get Document - success")
    void getDocumentSuccess() {
        ApplicationDocument doc = ApplicationDocument.builder().id(101L).documentName(TEST_PDF).build();
        when(documentRepository.findById(101L)).thenReturn(Optional.of(doc));

        DocumentResponse response = documentService.getDocument(101L);

        assertEquals(101L, response.getId());
    }

    @Test
    @DisplayName("Delete Document - success")
    void deleteDocumentSuccess(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve(TEST_PDF);
        Files.createFile(filePath);

        ApplicationDocument doc = ApplicationDocument.builder().id(101L).filePath(filePath.toString()).build();
        when(documentRepository.findById(101L)).thenReturn(Optional.of(doc));

        documentService.deleteDocument(101L);

        assertFalse(Files.exists(filePath));
        verify(documentRepository).delete(doc);
    }
}

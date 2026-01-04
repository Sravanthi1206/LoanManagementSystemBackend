package com.lms.loan.controller;

import com.lms.loan.dto.DocumentResponse;
import com.lms.loan.entity.ApplicationDocument;
import com.lms.loan.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@DisplayName("Document Controller Tests")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    @DisplayName("Upload Document - success")
    void uploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        DocumentResponse response = DocumentResponse.builder().id(101L).documentName("test.pdf").build();

        when(documentService.uploadDocument(eq(1L), any(ApplicationDocument.DocumentType.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/loans/1/documents")
                        .file(file)
                        .param("documentType", "IDENTITY_PROOF"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.documentName").value("test.pdf"));
    }

    @Test
    @DisplayName("Get Documents")
    void getDocuments() throws Exception {
        DocumentResponse response = DocumentResponse.builder().id(101L).documentName("test.pdf").build();
        when(documentService.getDocuments(1L)).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/loans/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101));
    }

    @Test
    @DisplayName("Get Document")
    void getDocument() throws Exception {
        DocumentResponse response = DocumentResponse.builder().id(101L).documentName("test.pdf").build();
        when(documentService.getDocument(101L)).thenReturn(response);

        mockMvc.perform(get("/loans/1/documents/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101));
    }

    @Test
    @DisplayName("Delete Document")
    void deleteDocument() throws Exception {
        mockMvc.perform(delete("/loans/1/documents/101"))
                .andExpect(status().isNoContent());

        verify(documentService).deleteDocument(101L);
    }
}

package com.lms.loan.dto;

import com.lms.loan.entity.ApplicationDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponse {
    private Long id;
    private Long applicationId;
    private ApplicationDocument.DocumentType documentType;
    private String documentName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
}

package com.aiknow.document.dto;

import com.aiknow.document.Document;
import com.aiknow.document.DocumentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID organizationId,
        UUID workspaceId,
        String name,
        String originalFilename,
        Long fileSize,
        String mimeType,
        DocumentStatus status,
        String errorMessage,
        Integer version,
        UUID uploadedBy,
        long chunkCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DocumentResponse from(Document doc, long chunkCount) {
        return new DocumentResponse(
                doc.getId(),
                doc.getOrganizationId(),
                doc.getWorkspaceId(),
                doc.getName(),
                doc.getOriginalFilename(),
                doc.getFileSize(),
                doc.getMimeType(),
                doc.getStatus(),
                doc.getErrorMessage(),
                doc.getVersion(),
                doc.getUploadedBy(),
                chunkCount,
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}

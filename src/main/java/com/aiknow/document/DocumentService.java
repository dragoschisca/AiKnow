package com.aiknow.document;

import com.aiknow.audit.AuditEventPublisher;
import com.aiknow.audit.AuditEventType;
import com.aiknow.exception.ErrorCode;
import com.aiknow.document.dto.DocumentResponse;
import com.aiknow.exception.BadRequestException;
import com.aiknow.exception.ResourceNotFoundException;
import com.aiknow.organization.OrganizationService;
import com.aiknow.subscription.UsageService;
import com.aiknow.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final FileStorageService fileStorageService;
    private final DocumentProcessingService documentProcessingService;
    private final OrganizationService organizationService;
    private final WorkspaceService workspaceService;
    private final UsageService usageService;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public Document uploadDocument(UUID orgId, UUID workspaceId, UUID userId, MultipartFile file) {
        organizationService.validateMembership(orgId, userId);
        workspaceService.getWorkspace(orgId, workspaceId);

        if (!"application/pdf".equals(file.getContentType())) {
            throw new BadRequestException("Invalid file type", ErrorCode.INVALID_FILE_TYPE);
        }
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty", ErrorCode.INVALID_FILE_TYPE);
        }

        usageService.checkAndIncrementDocumentUsage(orgId, file.getSize());

        try {
            String storagePath = fileStorageService.store(orgId, workspaceId, file.getOriginalFilename(), file.getInputStream());

            String name = file.getOriginalFilename();
            if (name != null && name.contains(".")) {
                name = name.substring(0, name.lastIndexOf('.'));
            }

            Document document = Document.builder()
                    .organizationId(orgId)
                    .workspaceId(workspaceId)
                    .name(name)
                    .originalFilename(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .storagePath(storagePath)
                    .status(DocumentStatus.UPLOADED)
                    .uploadedBy(userId)
                    .version(1)
                    .build();

            document = documentRepository.save(document);
            documentProcessingService.processDocument(document.getId());

            auditEventPublisher.publish(AuditEventType.DOCUMENT_UPLOADED, orgId, workspaceId, userId,
                    "DOCUMENT", document.getId(),
                    Map.of("filename", String.valueOf(file.getOriginalFilename()), "fileSize", file.getSize()));

            return document;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    public List<DocumentResponse> getDocuments(UUID orgId, UUID workspaceId, UUID userId) {
        organizationService.validateMembership(orgId, userId);
        return documentRepository.findByOrganizationIdAndWorkspaceId(orgId, workspaceId).stream()
                .map(doc -> {
                    long chunkCount = documentChunkRepository.countByDocumentId(doc.getId());
                    return DocumentResponse.from(doc, chunkCount);
                })
                .collect(Collectors.toList());
    }

    public DocumentResponse getDocument(UUID orgId, UUID workspaceId, UUID documentId, UUID userId) {
        organizationService.validateMembership(orgId, userId);
        Document document = documentRepository.findByIdAndOrganizationIdAndWorkspaceId(documentId, orgId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found", ErrorCode.DOCUMENT_NOT_FOUND));
        long chunkCount = documentChunkRepository.countByDocumentId(document.getId());
        return DocumentResponse.from(document, chunkCount);
    }

    @Transactional
    public void deleteDocument(UUID orgId, UUID workspaceId, UUID documentId, UUID userId) {
        organizationService.validateMembership(orgId, userId);
        Document document = documentRepository.findByIdAndOrganizationIdAndWorkspaceId(documentId, orgId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found", ErrorCode.DOCUMENT_NOT_FOUND));

        documentChunkRepository.deleteByDocumentId(documentId);
        fileStorageService.delete(document.getStoragePath());
        documentRepository.delete(document);
        usageService.decrementDocumentUsage(orgId, document.getFileSize());
        auditEventPublisher.publish(AuditEventType.DOCUMENT_DELETED, orgId, workspaceId, userId,
                "DOCUMENT", documentId, Map.of("filename", String.valueOf(document.getOriginalFilename())));
        log.info("Document deleted: {}", documentId);
    }

    @Transactional
    public void reprocessDocument(UUID orgId, UUID workspaceId, UUID documentId, UUID userId) {
        organizationService.validateMembership(orgId, userId);
        Document document = documentRepository.findByIdAndOrganizationIdAndWorkspaceId(documentId, orgId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found", ErrorCode.DOCUMENT_NOT_FOUND));

        if (document.getStatus() != DocumentStatus.READY && document.getStatus() != DocumentStatus.FAILED) {
            throw new BadRequestException("Document must be in READY or FAILED status", ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }

        documentChunkRepository.deleteByDocumentId(documentId);
        document.setStatus(DocumentStatus.UPLOADED);
        documentRepository.save(document);

        documentProcessingService.processDocument(documentId);
        auditEventPublisher.publish(AuditEventType.DOCUMENT_REPROCESSED, orgId, workspaceId, userId,
                "DOCUMENT", documentId, null);
    }
}

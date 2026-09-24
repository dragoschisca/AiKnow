package com.aiknow.document;

import com.aiknow.common.ApiResponse;
import com.aiknow.document.dto.DocumentResponse;
import com.aiknow.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/workspaces/{workspaceId}/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'MEMBER')")
    public ApiResponse<DocumentResponse> uploadDocument(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId,
            @RequestParam("file") MultipartFile file) {
        UUID userId = getCurrentUserId();
        Document document = documentService.uploadDocument(orgId, workspaceId, userId, file);
        return ApiResponse.success(DocumentResponse.from(document, 0));
    }

    @GetMapping
    public ApiResponse<List<DocumentResponse>> listDocuments(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId) {
        UUID userId = getCurrentUserId();
        return ApiResponse.success(documentService.getDocuments(orgId, workspaceId, userId));
    }

    @GetMapping("/{documentId}")
    public ApiResponse<DocumentResponse> getDocument(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId) {
        UUID userId = getCurrentUserId();
        return ApiResponse.success(documentService.getDocument(orgId, workspaceId, documentId, userId));
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'ADMIN')")
    public void deleteDocument(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId) {
        UUID userId = getCurrentUserId();
        documentService.deleteDocument(orgId, workspaceId, documentId, userId);
    }

    @PostMapping("/{documentId}/reprocess")
    @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'ADMIN')")
    public ApiResponse<Void> reprocessDocument(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId) {
        UUID userId = getCurrentUserId();
        documentService.reprocessDocument(orgId, workspaceId, documentId, userId);
        return ApiResponse.success(null);
    }
}

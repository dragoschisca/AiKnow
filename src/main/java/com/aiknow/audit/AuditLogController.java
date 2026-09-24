package com.aiknow.audit;

import com.aiknow.audit.dto.AuditLogResponse;
import com.aiknow.common.ApiResponse;
import com.aiknow.common.PagedResponse;
import com.aiknow.organization.OrganizationService;
import com.aiknow.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Audit log access is restricted to ADMIN and OWNER per the RBAC matrix.
 */
@RestController
@RequestMapping("/api/v1/organizations/{orgId}/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'ADMIN')")
    public ApiResponse<PagedResponse<AuditLogResponse>> list(
            @PathVariable UUID orgId,
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID userId = getCurrentUserId();
        organizationService.validateMembership(orgId, userId);

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        return ApiResponse.success(auditLogService.getAuditLogs(orgId, eventType, pageable));
    }

    private UUID getCurrentUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}

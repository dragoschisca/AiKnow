package com.aiknow.audit.dto;

import com.aiknow.audit.AuditLog;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID organizationId,
        UUID workspaceId,
        UUID actorUserId,
        String eventType,
        String resourceType,
        UUID resourceId,
        Map<String, Object> details,
        String ipAddress,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getOrganizationId(),
                log.getWorkspaceId(),
                log.getActorUserId(),
                log.getEventType(),
                log.getResourceType(),
                log.getResourceId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}

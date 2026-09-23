package com.aiknow.workspace.dto;

import com.aiknow.workspace.Workspace;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        UUID organizationId,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static WorkspaceResponse from(Workspace ws) {
        return new WorkspaceResponse(
                ws.getId(),
                ws.getOrganizationId(),
                ws.getName(),
                ws.getDescription(),
                ws.getCreatedAt()
        );
    }
}

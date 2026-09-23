package com.aiknow.organization.dto;

import com.aiknow.organization.Organization;
import com.aiknow.organization.OrganizationMemberRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        String role,
        LocalDateTime createdAt
) {
    public static OrganizationResponse from(Organization org, OrganizationMemberRole role) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getSlug(),
                role != null ? role.name() : null,
                org.getCreatedAt()
        );
    }
}

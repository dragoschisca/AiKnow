package com.aiknow.organization.dto;

import com.aiknow.organization.OrganizationMemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @NotNull
        UUID userId,
        
        @NotNull
        OrganizationMemberRole role
) {}

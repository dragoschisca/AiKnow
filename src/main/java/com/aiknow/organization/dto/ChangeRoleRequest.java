package com.aiknow.organization.dto;

import com.aiknow.organization.OrganizationMemberRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull
        OrganizationMemberRole role
) {
}

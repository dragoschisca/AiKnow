package com.aiknow.security;

import com.aiknow.organization.OrganizationMemberRepository;
import com.aiknow.organization.OrganizationMemberRole;
import com.aiknow.workspace.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Centralized RBAC evaluator exposed to method security as the "rbac" bean, e.g.
 * {@code @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'ADMIN')")}. This is the single source of
 * truth for role checks on the backend — authorization must never rely solely on the client.
 *
 * Role hierarchy (most to least privileged), based on declaration order of
 * {@link OrganizationMemberRole}: OWNER &gt; ADMIN &gt; MEMBER &gt; VIEWER. The same role enum
 * and hierarchy is reused for workspace-level membership.
 */
@Component("rbac")
@RequiredArgsConstructor
public class RbacEvaluator {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public boolean hasOrgRole(UUID orgId, String... roles) {
        OrganizationMemberRole actual = currentOrgRole(orgId);
        if (actual == null) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(r -> actual == OrganizationMemberRole.valueOf(r));
    }

    /**
     * True if the current user's organization role is at least as privileged as {@code minRole}.
     */
    public boolean hasMinOrgRole(UUID orgId, String minRole) {
        OrganizationMemberRole actual = currentOrgRole(orgId);
        if (actual == null) {
            return false;
        }
        return actual.ordinal() <= OrganizationMemberRole.valueOf(minRole).ordinal();
    }

    public boolean hasWorkspaceRole(UUID workspaceId, String... roles) {
        OrganizationMemberRole actual = currentWorkspaceRole(workspaceId);
        if (actual == null) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(r -> actual == OrganizationMemberRole.valueOf(r));
    }

    public boolean hasMinWorkspaceRole(UUID workspaceId, String minRole) {
        OrganizationMemberRole actual = currentWorkspaceRole(workspaceId);
        if (actual == null) {
            return false;
        }
        return actual.ordinal() <= OrganizationMemberRole.valueOf(minRole).ordinal();
    }

    private OrganizationMemberRole currentOrgRole(UUID orgId) {
        UUID userId = currentUserId();
        if (userId == null || orgId == null) {
            return null;
        }
        return organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .map(m -> m.getRole())
                .orElse(null);
    }

    private OrganizationMemberRole currentWorkspaceRole(UUID workspaceId) {
        UUID userId = currentUserId();
        if (userId == null || workspaceId == null) {
            return null;
        }
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(m -> m.getRole())
                .orElse(null);
    }

    private UUID currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof UserDetailsImpl userDetails ? userDetails.getId() : null;
    }
}

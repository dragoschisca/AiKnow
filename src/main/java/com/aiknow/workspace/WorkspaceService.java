package com.aiknow.workspace;

import com.aiknow.audit.AuditEventPublisher;
import com.aiknow.audit.AuditEventType;
import com.aiknow.exception.AccessDeniedException;
import com.aiknow.exception.DuplicateResourceException;
import com.aiknow.exception.ErrorCode;
import com.aiknow.exception.ResourceNotFoundException;
import com.aiknow.organization.OrganizationMemberRole;
import com.aiknow.organization.OrganizationService;
import com.aiknow.workspace.dto.WorkspaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final OrganizationService organizationService;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public Workspace createWorkspace(UUID orgId, String name, String description, UUID creatorUserId) {
        organizationService.validateMembership(orgId, creatorUserId);

        Workspace workspace = Workspace.builder()
                .organizationId(orgId)
                .name(name)
                .description(description)
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspaceId(savedWorkspace.getId())
                .userId(creatorUserId)
                .role(OrganizationMemberRole.OWNER)
                .build();
        workspaceMemberRepository.save(ownerMember);

        auditEventPublisher.publish(AuditEventType.WORKSPACE_CREATED, orgId, savedWorkspace.getId(), creatorUserId,
                "WORKSPACE", savedWorkspace.getId(), Map.of("name", name));

        return savedWorkspace;
    }

    public List<WorkspaceResponse> getWorkspaces(UUID orgId, UUID userId) {
        organizationService.validateMembership(orgId, userId);
        
        List<Workspace> workspaces = workspaceRepository.findByOrganizationId(orgId);
        return workspaces.stream()
                .map(WorkspaceResponse::from)
                .toList();
    }

    public Workspace getWorkspace(UUID orgId, UUID workspaceId) {
        return workspaceRepository.findByIdAndOrganizationId(workspaceId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found", ErrorCode.WORKSPACE_NOT_FOUND));
    }

    public Workspace getWorkspaceById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found", ErrorCode.WORKSPACE_NOT_FOUND));
    }

    @Transactional
    public WorkspaceMember addMember(UUID workspaceId, UUID userId, OrganizationMemberRole role, UUID actorUserId) {
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new DuplicateResourceException("User is already a member of this workspace", null);
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .role(role)
                .build();

        WorkspaceMember saved = workspaceMemberRepository.save(member);
        Workspace workspace = getWorkspaceById(workspaceId);
        auditEventPublisher.publish(AuditEventType.MEMBER_ADDED, workspace.getOrganizationId(), workspaceId, actorUserId,
                "WORKSPACE_MEMBER", saved.getId(), Map.of("targetUserId", userId.toString(), "role", role.name()));
        return saved;
    }

    public void validateMembership(UUID workspaceId, UUID userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Access denied: Not a member of this workspace");
        }
    }
}

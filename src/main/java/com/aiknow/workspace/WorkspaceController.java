package com.aiknow.workspace;

import com.aiknow.common.ApiResponse;
import com.aiknow.organization.OrganizationService;
import com.aiknow.organization.dto.AddMemberRequest;
import com.aiknow.security.UserDetailsImpl;
import com.aiknow.workspace.dto.CreateWorkspaceRequest;
import com.aiknow.workspace.dto.WorkspaceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'ADMIN')")
    public ApiResponse<WorkspaceResponse> create(@PathVariable UUID orgId, @Valid @RequestBody CreateWorkspaceRequest request) {
        UUID userId = getCurrentUser().getId();
        Workspace workspace = workspaceService.createWorkspace(orgId, request.name(), request.description(), userId);
        return ApiResponse.success(WorkspaceResponse.from(workspace));
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> list(@PathVariable UUID orgId) {
        UUID userId = getCurrentUser().getId();
        List<WorkspaceResponse> workspaces = workspaceService.getWorkspaces(orgId, userId);
        return ApiResponse.success(workspaces);
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkspaceResponse> get(@PathVariable UUID orgId, @PathVariable UUID id) {
        UUID userId = getCurrentUser().getId();
        organizationService.validateMembership(orgId, userId);
        Workspace workspace = workspaceService.getWorkspace(orgId, id);
        return ApiResponse.success(WorkspaceResponse.from(workspace));
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'ADMIN')")
    public ApiResponse<Void> addMember(@PathVariable UUID orgId, @PathVariable UUID id, @Valid @RequestBody AddMemberRequest request) {
        UUID userId = getCurrentUser().getId();
        organizationService.validateMembership(orgId, userId);

        workspaceService.addMember(id, request.userId(), request.role(), userId);
        return ApiResponse.success(null);
    }

    private UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

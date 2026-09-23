package com.aiknow.organization;

import com.aiknow.common.ApiResponse;
import com.aiknow.organization.dto.AddMemberRequest;
import com.aiknow.organization.dto.CreateOrganizationRequest;
import com.aiknow.organization.dto.OrganizationResponse;
import com.aiknow.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Slf4j
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrganizationResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        UUID userId = getCurrentUser().getId();
        Organization org = organizationService.createOrganization(request.name(), userId);
        OrganizationResponse response = OrganizationResponse.from(org, OrganizationMemberRole.OWNER);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<OrganizationResponse>> listOrganizations() {
        UUID userId = getCurrentUser().getId();
        List<OrganizationResponse> orgs = organizationService.getUserOrganizations(userId);
        return ApiResponse.success(orgs);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationResponse> getOrganization(@PathVariable UUID id) {
        UUID userId = getCurrentUser().getId();
        organizationService.validateMembership(id, userId);
        Organization org = organizationService.getOrganization(id);
        OrganizationMember membership = organizationService.getMembership(id, userId);
        return ApiResponse.success(OrganizationResponse.from(org, membership.getRole()));
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addMember(@PathVariable UUID id, @Valid @RequestBody AddMemberRequest request) {
        organizationService.addMember(id, request.userId(), request.role());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        organizationService.removeMember(id, userId);
        return ApiResponse.success(null);
    }

    private UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

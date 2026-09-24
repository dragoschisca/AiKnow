package com.aiknow.subscription;

import com.aiknow.common.ApiResponse;
import com.aiknow.organization.OrganizationService;
import com.aiknow.security.UserDetailsImpl;
import com.aiknow.subscription.dto.UsageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/usage")
@RequiredArgsConstructor
@Slf4j
public class UsageController {

    private final UsageService usageService;
    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("@rbac.hasMinOrgRole(#orgId, 'ADMIN')")
    public ApiResponse<UsageResponse> getUsage(@PathVariable UUID orgId) {
        UUID userId = getCurrentUserId();
        organizationService.validateMembership(orgId, userId);

        OrganizationUsage usage = usageService.getUsage(orgId);
        SubscriptionPlan plan = organizationService.getOrganization(orgId).getPlan();
        return ApiResponse.success(UsageResponse.from(usage, plan));
    }

    private UUID getCurrentUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}

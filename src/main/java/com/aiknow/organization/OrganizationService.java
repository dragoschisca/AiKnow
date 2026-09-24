package com.aiknow.organization;

import com.aiknow.audit.AuditEventPublisher;
import com.aiknow.audit.AuditEventType;
import com.aiknow.exception.AccessDeniedException;
import com.aiknow.exception.DuplicateResourceException;
import com.aiknow.exception.ErrorCode;
import com.aiknow.exception.ResourceNotFoundException;
import com.aiknow.organization.dto.OrganizationResponse;
import com.aiknow.user.UserRepository;
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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public Organization createOrganization(String name, UUID creatorUserId) {
        String slug = generateSlug(name);
        if (organizationRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Organization with this slug already exists", ErrorCode.ORG_ALREADY_EXISTS);
        }

        Organization organization = Organization.builder()
                .name(name)
                .slug(slug)
                .build();

        Organization savedOrg = organizationRepository.save(organization);

        OrganizationMember ownerMember = OrganizationMember.builder()
                .organizationId(savedOrg.getId())
                .userId(creatorUserId)
                .role(OrganizationMemberRole.OWNER)
                .build();
        organizationMemberRepository.save(ownerMember);

        auditEventPublisher.publish(AuditEventType.ORGANIZATION_CREATED, savedOrg.getId(), null, creatorUserId,
                "ORGANIZATION", savedOrg.getId(), Map.of("name", name));

        return savedOrg;
    }

    public List<OrganizationResponse> getUserOrganizations(UUID userId) {
        List<OrganizationMember> memberships = organizationMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(member -> {
                    Organization org = getOrganization(member.getOrganizationId());
                    return OrganizationResponse.from(org, member.getRole());
                })
                .toList();
    }

    public Organization getOrganization(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found", ErrorCode.ORG_NOT_FOUND));
    }

    @Transactional
    public OrganizationMember addMember(UUID orgId, UUID userId, OrganizationMemberRole role, UUID actorUserId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found", ErrorCode.ORG_NOT_FOUND);
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found", null);
        }
        if (organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, userId)) {
            throw new DuplicateResourceException("User is already a member", ErrorCode.ORG_MEMBER_EXISTS);
        }

        OrganizationMember member = OrganizationMember.builder()
                .organizationId(orgId)
                .userId(userId)
                .role(role)
                .build();

        OrganizationMember saved = organizationMemberRepository.save(member);
        auditEventPublisher.publish(AuditEventType.MEMBER_ADDED, orgId, null, actorUserId,
                "ORGANIZATION_MEMBER", saved.getId(), Map.of("targetUserId", userId.toString(), "role", role.name()));
        return saved;
    }

    @Transactional
    public void removeMember(UUID orgId, UUID userId, UUID actorUserId) {
        OrganizationMember membership = getMembership(orgId, userId);
        organizationMemberRepository.delete(membership);
        auditEventPublisher.publish(AuditEventType.MEMBER_REMOVED, orgId, null, actorUserId,
                "ORGANIZATION_MEMBER", membership.getId(),
                Map.of("targetUserId", userId.toString(), "role", membership.getRole().name()));
    }

    @Transactional
    public OrganizationMember changeMemberRole(UUID orgId, UUID userId, OrganizationMemberRole newRole, UUID actorUserId) {
        OrganizationMember membership = getMembership(orgId, userId);
        OrganizationMemberRole previousRole = membership.getRole();
        membership.setRole(newRole);
        OrganizationMember saved = organizationMemberRepository.save(membership);
        auditEventPublisher.publish(AuditEventType.ROLE_CHANGED, orgId, null, actorUserId,
                "ORGANIZATION_MEMBER", saved.getId(),
                Map.of("targetUserId", userId.toString(), "from", previousRole.name(), "to", newRole.name()));
        return saved;
    }

    public OrganizationMember getMembership(UUID orgId, UUID userId) {
        return organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found", null));
    }

    public void validateMembership(UUID orgId, UUID userId) {
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(orgId, userId)) {
            throw new AccessDeniedException("Access denied: Not a member of this organization");
        }
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("^-+|-+$", "");
        if (slug.length() > 50) {
            slug = slug.substring(0, 50);
            slug = slug.replaceAll("-+$", "");
        }
        return slug;
    }
}

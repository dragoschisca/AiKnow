package com.aiknow.subscription;

import com.aiknow.organization.Organization;
import com.aiknow.organization.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Tracks and enforces per-organization plan limits (documents, storage, monthly questions).
 * Writes are guarded with a pessimistic row lock to stay correct under concurrent uploads
 * or chat requests within the same organization.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UsageService {

    private final OrganizationUsageRepository usageRepository;
    private final OrganizationService organizationService;

    @Transactional
    public void checkAndIncrementDocumentUsage(UUID orgId, long fileSizeBytes) {
        SubscriptionPlan plan = organizationService.getOrganization(orgId).getPlan();
        OrganizationUsage usage = getOrCreateUsageLocked(orgId);

        if (usage.getDocumentCount() + 1 > plan.getMaxDocuments()) {
            throw new UsageLimitExceededException(
                    "Document limit reached for the " + plan + " plan (" + plan.getMaxDocuments() + " documents)");
        }
        if (usage.getStorageBytes() + fileSizeBytes > plan.getMaxStorageBytes()) {
            throw new UsageLimitExceededException(
                    "Storage limit reached for the " + plan + " plan");
        }

        usage.setDocumentCount(usage.getDocumentCount() + 1);
        usage.setStorageBytes(usage.getStorageBytes() + fileSizeBytes);
        usageRepository.save(usage);
    }

    @Transactional
    public void decrementDocumentUsage(UUID orgId, long fileSizeBytes) {
        OrganizationUsage usage = getOrCreateUsageLocked(orgId);
        usage.setDocumentCount(Math.max(0, usage.getDocumentCount() - 1));
        usage.setStorageBytes(Math.max(0, usage.getStorageBytes() - fileSizeBytes));
        usageRepository.save(usage);
    }

    @Transactional
    public void checkAndIncrementQuestionUsage(UUID orgId) {
        SubscriptionPlan plan = organizationService.getOrganization(orgId).getPlan();
        OrganizationUsage usage = getOrCreateUsageLocked(orgId);
        resetIfDue(usage);

        if (usage.getQuestionsThisMonth() + 1 > plan.getMaxQuestionsPerMonth()) {
            throw new UsageLimitExceededException(
                    "Monthly question limit reached for the " + plan + " plan (" + plan.getMaxQuestionsPerMonth() + " questions)");
        }

        usage.setQuestionsThisMonth(usage.getQuestionsThisMonth() + 1);
        usageRepository.save(usage);
    }

    @Transactional
    public OrganizationUsage getUsage(UUID orgId) {
        OrganizationUsage usage = getOrCreateUsage(orgId);
        resetIfDue(usage);
        return usageRepository.save(usage);
    }

    private OrganizationUsage getOrCreateUsageLocked(UUID orgId) {
        return usageRepository.findWithLockByOrganizationId(orgId)
                .orElseGet(() -> createUsage(orgId));
    }

    private OrganizationUsage getOrCreateUsage(UUID orgId) {
        return usageRepository.findByOrganizationId(orgId)
                .orElseGet(() -> createUsage(orgId));
    }

    private OrganizationUsage createUsage(UUID orgId) {
        Organization organization = organizationService.getOrganization(orgId);
        log.info("Initializing usage tracking for organization {}", organization.getId());
        return usageRepository.save(OrganizationUsage.builder()
                .organizationId(orgId)
                .documentCount(0)
                .storageBytes(0L)
                .questionsThisMonth(0)
                .resetDate(firstOfNextMonth())
                .build());
    }

    private void resetIfDue(OrganizationUsage usage) {
        if (!LocalDate.now().isBefore(usage.getResetDate())) {
            usage.setQuestionsThisMonth(0);
            usage.setResetDate(firstOfNextMonth());
        }
    }

    private LocalDate firstOfNextMonth() {
        return LocalDate.now().withDayOfMonth(1).plusMonths(1);
    }
}

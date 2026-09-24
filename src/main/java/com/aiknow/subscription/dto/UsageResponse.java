package com.aiknow.subscription.dto;

import com.aiknow.subscription.OrganizationUsage;
import com.aiknow.subscription.SubscriptionPlan;

import java.time.LocalDate;

public record UsageResponse(
        SubscriptionPlan plan,
        int documentCount,
        int maxDocuments,
        long storageBytes,
        long maxStorageBytes,
        int questionsThisMonth,
        int maxQuestionsPerMonth,
        LocalDate resetDate
) {
    public static UsageResponse from(OrganizationUsage usage, SubscriptionPlan plan) {
        return new UsageResponse(
                plan,
                usage.getDocumentCount(),
                plan.getMaxDocuments(),
                usage.getStorageBytes(),
                plan.getMaxStorageBytes(),
                usage.getQuestionsThisMonth(),
                plan.getMaxQuestionsPerMonth(),
                usage.getResetDate()
        );
    }
}

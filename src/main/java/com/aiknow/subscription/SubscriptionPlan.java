package com.aiknow.subscription;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {

    FREE(10, 500L * 1024 * 1024, 50),
    STARTER(100, 5L * 1024 * 1024 * 1024, 500),
    BUSINESS(1_000, 50L * 1024 * 1024 * 1024, 5_000),
    ENTERPRISE(Integer.MAX_VALUE, Long.MAX_VALUE, Integer.MAX_VALUE);

    private final int maxDocuments;
    private final long maxStorageBytes;
    private final int maxQuestionsPerMonth;
}

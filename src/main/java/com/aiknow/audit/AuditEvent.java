package com.aiknow.audit;

import java.util.Map;
import java.util.UUID;

/**
 * Lightweight application event carrying everything needed to persist an {@link AuditLog}.
 * IP address is captured synchronously by the publisher (from the servlet request), since the
 * listener runs asynchronously on a worker thread with no request context.
 */
public record AuditEvent(
        AuditEventType eventType,
        UUID organizationId,
        UUID workspaceId,
        UUID actorUserId,
        String resourceType,
        UUID resourceId,
        Map<String, Object> details,
        String ipAddress
) {
}

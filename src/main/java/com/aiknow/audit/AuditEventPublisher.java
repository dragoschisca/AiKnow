package com.aiknow.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.UUID;

/**
 * Entry point business services use to record an audit trail event. Publishing is synchronous
 * and cheap (it only fires a Spring application event); the actual database write happens
 * asynchronously in {@link AuditEventListener}, after the caller's transaction commits, so
 * audit logging never adds latency to or risks failing the business operation it records.
 */
@Component
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(AuditEventType eventType, UUID organizationId, UUID workspaceId, UUID actorUserId,
                         String resourceType, UUID resourceId, Map<String, Object> details) {
        eventPublisher.publishEvent(new AuditEvent(
                eventType, organizationId, workspaceId, actorUserId, resourceType, resourceId, details, currentIpAddress()));
    }

    private String currentIpAddress() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletAttrs)) {
            return null;
        }
        HttpServletRequest request = servletAttrs.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

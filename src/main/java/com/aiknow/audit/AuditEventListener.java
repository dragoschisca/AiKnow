package com.aiknow.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Persists {@link AuditEvent}s to the {@code audit_logs} table asynchronously, after the
 * publishing transaction has committed (so a rolled-back operation never leaves a misleading
 * audit entry behind). Falls back to immediate execution when no transaction is active (e.g.
 * login, which isn't wrapped in {@code @Transactional}).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAuditEvent(AuditEvent event) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .organizationId(event.organizationId())
                    .workspaceId(event.workspaceId())
                    .actorUserId(event.actorUserId())
                    .eventType(event.eventType().name())
                    .resourceType(event.resourceType())
                    .resourceId(event.resourceId())
                    .details(event.details())
                    .ipAddress(event.ipAddress())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Audit logging must never break the business flow it observes.
            log.error("Failed to persist audit log for event {}", event.eventType(), e);
        }
    }
}

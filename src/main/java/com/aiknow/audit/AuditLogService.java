package com.aiknow.audit;

import com.aiknow.audit.dto.AuditLogResponse;
import com.aiknow.common.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public PagedResponse<AuditLogResponse> getAuditLogs(UUID orgId, AuditEventType eventType, Pageable pageable) {
        Page<AuditLog> page = eventType != null
                ? auditLogRepository.findByOrganizationIdAndEventTypeOrderByCreatedAtDesc(orgId, eventType.name(), pageable)
                : auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId, pageable);
        return PagedResponse.of(page.map(AuditLogResponse::from));
    }
}

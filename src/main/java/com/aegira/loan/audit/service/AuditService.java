package com.aegira.loan.audit.service;

import com.aegira.loan.audit.entity.AuditLog;
import com.aegira.loan.audit.repository.AuditLogRepository;
import com.aegira.loan.common.security.PiiRedactionUtil;
import com.aegira.loan.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditLog log(String entityType, UUID entityId, String action, User performedBy,
                        String oldValue, String newValue, String notes, String correlationId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setPerformedBy(performedBy);
        auditLog.setOldValue(PiiRedactionUtil.sanitize(oldValue));
        auditLog.setNewValue(PiiRedactionUtil.sanitize(newValue));
        auditLog.setNotes(PiiRedactionUtil.sanitize(notes));
        auditLog.setCorrelationId(correlationId);
        log.info("event_name=audit_recorded action={} entity_type={} resource_id={}", action, entityType, entityId);
        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> forLoanApplication(UUID applicationId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc("LOAN_APPLICATION", applicationId);
    }
}

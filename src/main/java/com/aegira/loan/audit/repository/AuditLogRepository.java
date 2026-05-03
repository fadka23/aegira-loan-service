package com.aegira.loan.audit.repository;

import com.aegira.loan.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtAsc(String entityType, UUID entityId);
}

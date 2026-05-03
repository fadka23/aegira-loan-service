package com.aegira.loan.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponse {
    private UUID id;
    private String entityType;
    private UUID entityId;
    private String action;
    private UUID performedBy;
    private String oldValue;
    private String newValue;
    private String notes;
    private String correlationId;
    private OffsetDateTime createdAt;
}

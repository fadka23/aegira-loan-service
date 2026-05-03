package com.aegira.loan.audit.controller;

import com.aegira.loan.audit.dto.AuditLogResponse;
import com.aegira.loan.audit.entity.AuditLog;
import com.aegira.loan.audit.service.AuditService;
import com.aegira.loan.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-applications/{id}/audit-logs")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> logs(@PathVariable UUID id) {
        List<AuditLogResponse> responses = auditService.forLoanApplication(id).stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(responses);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .performedBy(log.getPerformedBy() == null ? null : log.getPerformedBy().getId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .notes(log.getNotes())
                .correlationId(log.getCorrelationId())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

package com.aegira.loan.approval.dto;

import com.aegira.loan.approval.entity.ApprovalDecision;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ApprovalHistoryResponse {
    private UUID id;
    private UUID loanApplicationId;
    private UUID performedBy;
    private ApprovalDecision decision;
    private BigDecimal approvedAmount;
    private String notes;
    private String correlationId;
    private OffsetDateTime createdAt;
}

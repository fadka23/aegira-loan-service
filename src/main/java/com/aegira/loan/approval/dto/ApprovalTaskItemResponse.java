package com.aegira.loan.approval.dto;

import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ApprovalTaskItemResponse {
    private UUID id;
    private String applicationNumber;
    private String customerName;
    private BigDecimal requestedAmount;
    private BigDecimal projectedDsr;
    private RiskLevel riskLevel;
    private ApplicationStatus status;
    private OffsetDateTime submittedAt;
}

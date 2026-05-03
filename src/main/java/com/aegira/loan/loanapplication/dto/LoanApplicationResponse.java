package com.aegira.loan.loanapplication.dto;

import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanApplicationResponse {
    private UUID id;
    private String applicationNumber;
    private UUID customerId;
    private UUID agentId;
    private UUID loanProductId;
    private BigDecimal requestedAmount;
    private Integer requestedTenure;
    private String loanPurpose;
    private ApplicationStatus status;
    private RiskLevel riskLevel;
    private OffsetDateTime submittedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;
}

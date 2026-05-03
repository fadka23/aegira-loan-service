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
public class LoanApplicationListItemResponse {
    private UUID id;
    private String applicationNumber;
    private String customerName;
    private String customerNik;
    private BigDecimal requestedAmount;
    private Integer requestedTenure;
    private BigDecimal monthlyInstallment;
    private BigDecimal projectedDsr;
    private ApplicationStatus status;
    private RiskLevel riskLevel;
    private OffsetDateTime createdAt;
}

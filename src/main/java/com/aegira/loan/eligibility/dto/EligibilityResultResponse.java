package com.aegira.loan.eligibility.dto;

import com.aegira.loan.eligibility.entity.EligibilityRule;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class EligibilityResultResponse {
    private UUID id;
    private UUID loanApplicationId;
    private EligibilityRule ruleName;
    private Boolean passed;
    private String message;
    private OffsetDateTime createdAt;
}

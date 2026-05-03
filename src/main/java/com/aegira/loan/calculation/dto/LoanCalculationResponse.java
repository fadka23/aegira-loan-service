package com.aegira.loan.calculation.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanCalculationResponse {
    private UUID id;
    private UUID loanApplicationId;
    private BigDecimal loanAmount;
    private BigDecimal annualInterestRate;
    private Integer tenureMonth;
    private BigDecimal totalInterest;
    private BigDecimal totalPayment;
    private BigDecimal monthlyInstallment;
    private BigDecimal existingInstallment;
    private BigDecimal monthlyIncome;
    private BigDecimal currentDsr;
    private BigDecimal projectedDsr;
    private BigDecimal maximumDsr;
    private Boolean eligible;
    private OffsetDateTime createdAt;
}

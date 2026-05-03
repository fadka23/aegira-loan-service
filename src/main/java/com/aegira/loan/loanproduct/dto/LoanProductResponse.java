package com.aegira.loan.loanproduct.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanProductResponse {
    private UUID id;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTenure;
    private Integer maxTenure;
    private BigDecimal annualInterestRate;
    private BigDecimal minimumIncome;
    private BigDecimal maximumDsr;
    private Boolean needCollateral;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

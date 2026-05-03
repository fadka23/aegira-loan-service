package com.aegira.loan.options.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class LoanProductOptionResponse {
    private UUID id;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTenure;
    private Integer maxTenure;
}

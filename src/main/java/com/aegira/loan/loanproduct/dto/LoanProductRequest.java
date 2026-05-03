package com.aegira.loan.loanproduct.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class LoanProductRequest {
    @NotBlank
    private String name;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minAmount;
    @NotNull
    private BigDecimal maxAmount;
    @NotNull
    private Integer minTenure;
    @NotNull
    private Integer maxTenure;
    @NotNull
    private BigDecimal annualInterestRate;
    @NotNull
    private BigDecimal minimumIncome;
    @NotNull
    private BigDecimal maximumDsr;
    @NotNull
    private Boolean needCollateral;
    @NotNull
    private Boolean active;
}

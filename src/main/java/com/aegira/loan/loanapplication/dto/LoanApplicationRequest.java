package com.aegira.loan.loanapplication.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LoanApplicationRequest {
    @NotNull
    private UUID customerId;
    @NotNull
    private UUID loanProductId;
    @NotNull
    private BigDecimal requestedAmount;
    @NotNull
    private Integer requestedTenure;
    @NotBlank
    private String loanPurpose;
}

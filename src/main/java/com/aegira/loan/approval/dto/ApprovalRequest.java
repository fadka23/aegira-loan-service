package com.aegira.loan.approval.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApprovalRequest {
    private BigDecimal approvedAmount;
    private String notes;
}

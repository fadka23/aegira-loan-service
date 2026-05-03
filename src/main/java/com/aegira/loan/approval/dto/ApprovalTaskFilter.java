package com.aegira.loan.approval.dto;

import lombok.Data;

@Data
public class ApprovalTaskFilter {
    private int page = 0;
    private int size = 10;
    private String role;
}

package com.aegira.loan.loanapplication.dto;

import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanApplicationListFilter {
    private int page = 0;
    private int size = 10;
    private ApplicationStatus status;
    private RiskLevel riskLevel;
    private String customerName;
    private String applicationNumber;
    private LocalDate fromDate;
    private LocalDate toDate;
}

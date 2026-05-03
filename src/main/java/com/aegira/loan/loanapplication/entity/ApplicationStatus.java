package com.aegira.loan.loanapplication.entity;

public enum ApplicationStatus {
    DRAFT,
    WAITING_RISK_REVIEW,
    RISK_APPROVED,
    RISK_REJECTED,
    REVISION_REQUESTED,
    WAITING_HO_APPROVAL,
    HO_APPROVED,
    HO_REJECTED,
    CANCELLED
}

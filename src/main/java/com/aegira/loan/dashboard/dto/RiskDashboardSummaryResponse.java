package com.aegira.loan.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskDashboardSummaryResponse {
    private long totalWaitingRiskReview;
    private long totalMediumRisk;
    private long totalHighRisk;
    private long totalApprovedToday;
    private long totalRejectedToday;
}

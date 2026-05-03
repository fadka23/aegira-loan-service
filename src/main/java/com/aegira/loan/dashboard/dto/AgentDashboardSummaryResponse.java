package com.aegira.loan.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentDashboardSummaryResponse {
    private long totalDraft;
    private long totalWaitingRiskReview;
    private long totalApproved;
    private long totalRejected;
    private long totalRevisionRequested;
}

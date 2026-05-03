package com.aegira.loan.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HoDashboardSummaryResponse {
    private long totalWaitingHoApproval;
    private long totalApprovedToday;
    private long totalRejectedToday;
}

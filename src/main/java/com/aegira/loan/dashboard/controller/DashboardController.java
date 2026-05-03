package com.aegira.loan.dashboard.controller;

import com.aegira.loan.common.dto.ApiResponse;
import com.aegira.loan.dashboard.dto.AgentDashboardSummaryResponse;
import com.aegira.loan.dashboard.dto.HoDashboardSummaryResponse;
import com.aegira.loan.dashboard.dto.RiskDashboardSummaryResponse;
import com.aegira.loan.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/agent/summary")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Agent dashboard summary", description = "Returns application counts for the current agent only.")
    public ApiResponse<AgentDashboardSummaryResponse> agentSummary() {
        return ApiResponse.success(dashboardService.agentSummary());
    }

    @GetMapping("/risk/summary")
    @PreAuthorize("hasRole('RISK')")
    @Operation(summary = "Risk dashboard summary", description = "Returns waiting review and risk workload metrics for Risk Officer UI.")
    public ApiResponse<RiskDashboardSummaryResponse> riskSummary() {
        return ApiResponse.success(dashboardService.riskSummary());
    }

    @GetMapping("/ho/summary")
    @PreAuthorize("hasRole('HO')")
    @Operation(summary = "HO dashboard summary", description = "Returns Head Office approval workload metrics for HO UI.")
    public ApiResponse<HoDashboardSummaryResponse> hoSummary() {
        return ApiResponse.success(dashboardService.hoSummary());
    }
}

package com.aegira.loan.approval.controller;

import com.aegira.loan.approval.dto.ApprovalHistoryResponse;
import com.aegira.loan.approval.dto.ApprovalRequest;
import com.aegira.loan.approval.service.ApprovalService;
import com.aegira.loan.common.dto.ApiResponse;
import com.aegira.loan.common.idempotency.RequireIdempotency;
import com.aegira.loan.loanapplication.dto.LoanApplicationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-applications/{id}")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalService approvalService;
    private static final String IDEMPOTENCY_DESCRIPTION = "This endpoint is protected by Redis-based idempotency. "
            + "Clients must send a unique Idempotency-Key for each new approval action. "
            + "Reusing the same key will be treated as a duplicate request.";

    @PostMapping("/risk/approve")
    @PreAuthorize("hasRole('RISK')")
    @RequireIdempotency
    @Operation(summary = "Risk approve loan application", description = IDEMPOTENCY_DESCRIPTION,
            parameters = @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Unique key for this risk approval request."))
    public ApiResponse<LoanApplicationResponse> riskApprove(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
        return ApiResponse.success(approvalService.riskApprove(id, request));
    }

    @PostMapping("/risk/reject")
    @PreAuthorize("hasRole('RISK')")
    @RequireIdempotency
    @Operation(summary = "Risk reject loan application", description = IDEMPOTENCY_DESCRIPTION,
            parameters = @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Unique key for this risk rejection request."))
    public ApiResponse<LoanApplicationResponse> riskReject(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
        return ApiResponse.success(approvalService.riskReject(id, request));
    }

    @PostMapping("/risk/request-revision")
    @PreAuthorize("hasRole('RISK')")
    @RequireIdempotency
    @Operation(summary = "Risk request revision", description = IDEMPOTENCY_DESCRIPTION,
            parameters = @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Unique key for this risk revision request."))
    public ApiResponse<LoanApplicationResponse> requestRevision(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
        return ApiResponse.success(approvalService.requestRevision(id, request));
    }

    @PostMapping("/ho/approve")
    @PreAuthorize("hasRole('HO')")
    @RequireIdempotency
    @Operation(summary = "HO approve loan application", description = IDEMPOTENCY_DESCRIPTION,
            parameters = @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Unique key for this HO approval request."))
    public ApiResponse<LoanApplicationResponse> hoApprove(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
        return ApiResponse.success(approvalService.hoApprove(id, request));
    }

    @PostMapping("/ho/reject")
    @PreAuthorize("hasRole('HO')")
    @RequireIdempotency
    @Operation(summary = "HO reject loan application", description = IDEMPOTENCY_DESCRIPTION,
            parameters = @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Unique key for this HO rejection request."))
    public ApiResponse<LoanApplicationResponse> hoReject(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
        return ApiResponse.success(approvalService.hoReject(id, request));
    }

    @GetMapping("/approval-histories")
    public ApiResponse<List<ApprovalHistoryResponse>> histories(@PathVariable UUID id) {
        return ApiResponse.success(approvalService.histories(id));
    }
}

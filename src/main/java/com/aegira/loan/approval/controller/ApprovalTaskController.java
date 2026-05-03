package com.aegira.loan.approval.controller;

import com.aegira.loan.approval.dto.ApprovalTaskFilter;
import com.aegira.loan.approval.dto.ApprovalTaskItemResponse;
import com.aegira.loan.approval.service.ApprovalTaskService;
import com.aegira.loan.common.dto.ApiResponse;
import com.aegira.loan.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/approval-tasks")
@RequiredArgsConstructor
public class ApprovalTaskController {
    private final ApprovalTaskService approvalTaskService;

    @GetMapping
    @Operation(summary = "Approval task list",
            description = "Returns approval work queue for the current approver. RISK receives WAITING_RISK_REVIEW applications. HO receives WAITING_HO_APPROVAL applications.")
    public ApiResponse<PageResponse<ApprovalTaskItemResponse>> tasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role) {
        ApprovalTaskFilter filter = new ApprovalTaskFilter();
        filter.setPage(page);
        filter.setSize(size);
        filter.setRole(role);
        return ApiResponse.success(approvalTaskService.tasks(filter));
    }
}

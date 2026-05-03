package com.aegira.loan.loanapplication.controller;

import com.aegira.loan.calculation.dto.LoanCalculationResponse;
import com.aegira.loan.calculation.service.LoanCalculationService;
import com.aegira.loan.common.dto.ApiResponse;
import com.aegira.loan.common.idempotency.RequireIdempotency;
import com.aegira.loan.eligibility.dto.EligibilityResultResponse;
import com.aegira.loan.eligibility.service.EligibilityService;
import com.aegira.loan.loanapplication.dto.LoanApplicationRequest;
import com.aegira.loan.loanapplication.dto.LoanApplicationResponse;
import com.aegira.loan.loanapplication.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {
    private final LoanApplicationService loanApplicationService;
    private final LoanCalculationService loanCalculationService;
    private final EligibilityService eligibilityService;

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<LoanApplicationResponse> create(@Valid @RequestBody LoanApplicationRequest request) {
        return ApiResponse.success(loanApplicationService.create(request));
    }

    @GetMapping
    public ApiResponse<List<LoanApplicationResponse>> all() {
        return ApiResponse.success(loanApplicationService.findAllVisible());
    }

    @GetMapping("/{id}")
    public ApiResponse<LoanApplicationResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(loanApplicationService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<LoanApplicationResponse> update(@PathVariable UUID id, @Valid @RequestBody LoanApplicationRequest request) {
        return ApiResponse.success(loanApplicationService.update(id, request));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('AGENT')")
    @RequireIdempotency
    @Operation(summary = "Submit loan application",
            description = "The submit endpoint calculates loan installment, current DSR, projected DSR, eligibility, and risk level. "
                    + "The data source can be controlled by feature flag loan.data-source.mode. "
                    + "MOCK mode uses mock customer and loan product data. DATABASE mode uses PostgreSQL data. "
                    + "CustomerID is used as correlationId for debugging and audit traceability. "
                    + "This endpoint is protected by Redis-based idempotency. Clients must send a unique Idempotency-Key for each new submit action. "
                    + "Reusing the same key will be treated as a duplicate request.",
            parameters = @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true,
                    description = "Unique key for this submit request."))
    public ApiResponse<LoanApplicationResponse> submit(@PathVariable UUID id) {
        return ApiResponse.success(loanApplicationService.submit(id));
    }

    @GetMapping("/{id}/calculation")
    public ApiResponse<LoanCalculationResponse> calculation(@PathVariable UUID id) {
        loanApplicationService.getVisible(id);
        return ApiResponse.success(loanCalculationService.latest(id));
    }

    @GetMapping("/{id}/eligibility")
    public ApiResponse<List<EligibilityResultResponse>> eligibility(@PathVariable UUID id) {
        loanApplicationService.getVisible(id);
        return ApiResponse.success(eligibilityService.findByApplication(id));
    }
}

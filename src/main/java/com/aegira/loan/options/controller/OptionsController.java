package com.aegira.loan.options.controller;

import com.aegira.loan.common.dto.ApiResponse;
import com.aegira.loan.options.dto.CustomerOptionResponse;
import com.aegira.loan.options.dto.LoanProductOptionResponse;
import com.aegira.loan.options.service.OptionsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
public class OptionsController {
    private final OptionsService optionsService;

    @GetMapping("/loan-products")
    @Operation(summary = "Loan product dropdown options", description = "Returns active loan products for frontend dropdown controls.")
    public ApiResponse<List<LoanProductOptionResponse>> loanProducts() {
        return ApiResponse.success(optionsService.loanProducts());
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Customer dropdown options", description = "Returns customer options for agent UI. MVP returns all customers because customer ownership is not modeled yet.")
    public ApiResponse<List<CustomerOptionResponse>> customers(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(optionsService.customers(keyword));
    }
}

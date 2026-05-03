package com.aegira.loan.loanproduct.controller;

import com.aegira.loan.common.dto.ApiResponse;
import com.aegira.loan.loanproduct.dto.LoanProductRequest;
import com.aegira.loan.loanproduct.dto.LoanProductResponse;
import com.aegira.loan.loanproduct.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/loan-products")
@RequiredArgsConstructor
public class LoanProductController {
    private final LoanProductService loanProductService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LoanProductResponse> create(@Valid @RequestBody LoanProductRequest request) {
        return ApiResponse.success(loanProductService.create(request));
    }

    @GetMapping
    public ApiResponse<List<LoanProductResponse>> all() {
        return ApiResponse.success(loanProductService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<LoanProductResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(loanProductService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LoanProductResponse> update(@PathVariable UUID id, @Valid @RequestBody LoanProductRequest request) {
        return ApiResponse.success(loanProductService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        loanProductService.delete(id);
        return ApiResponse.success(null);
    }
}

package com.aegira.loan.customer.controller;

import com.aegira.loan.common.dto.ApiResponse;
import com.aegira.loan.customer.dto.CustomerRequest;
import com.aegira.loan.customer.dto.CustomerResponse;
import com.aegira.loan.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ApiResponse.success(customerService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','RISK','HO','ADMIN')")
    public ApiResponse<List<CustomerResponse>> all() {
        return ApiResponse.success(customerService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','RISK','HO','ADMIN')")
    public ApiResponse<CustomerResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(customerService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ApiResponse<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return ApiResponse.success(customerService.update(id, request));
    }

    @GetMapping("/check-duplicate")
    @PreAuthorize("hasAnyRole('AGENT','RISK','HO','ADMIN')")
    public ApiResponse<Map<String, Boolean>> duplicate(@RequestParam String nik) {
        return ApiResponse.success(Collections.singletonMap("duplicate", customerService.duplicateNik(nik)));
    }
}

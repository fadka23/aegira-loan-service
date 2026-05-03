package com.aegira.loan.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerResponse {
    private UUID id;
    private String nik;
    private String name;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String maritalStatus;
    private String jobType;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal existingInstallment;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

package com.aegira.loan.customer.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerRequest {
    @Pattern(regexp = "\\d{16}")
    private String nik;
    @NotBlank
    private String name;
    private String phoneNumber;
    @NotNull
    private LocalDate dateOfBirth;
    @NotBlank
    private String address;
    private String maritalStatus;
    private String jobType;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monthlyIncome;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monthlyExpense;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal existingInstallment;
}

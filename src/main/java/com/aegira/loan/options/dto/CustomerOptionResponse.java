package com.aegira.loan.options.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CustomerOptionResponse {
    private UUID id;
    private String name;
    private String nik;
    private String phoneNumber;
}

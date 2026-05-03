package com.aegira.loan.auth.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}

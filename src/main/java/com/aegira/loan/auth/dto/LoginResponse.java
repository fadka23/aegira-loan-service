package com.aegira.loan.auth.dto;

import com.aegira.loan.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UUID userId;
    private String email;
    private Role role;
}

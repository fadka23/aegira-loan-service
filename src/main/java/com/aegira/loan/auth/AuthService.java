package com.aegira.loan.auth;

import com.aegira.loan.auth.dto.LoginRequest;
import com.aegira.loan.auth.dto.LoginResponse;
import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.user.entity.User;
import com.aegira.loan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityUtil securityUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }
        return new LoginResponse(jwtTokenProvider.generate(user), user.getId(), user.getEmail(), user.getRole());
    }

    public LoginResponse me() {
        User user = securityUtil.currentUser();
        return new LoginResponse(null, user.getId(), user.getEmail(), user.getRole());
    }
}

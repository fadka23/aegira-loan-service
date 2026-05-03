package com.aegira.loan.common.security;

import com.aegira.loan.common.exception.ForbiddenException;
import com.aegira.loan.common.exception.NotFoundException;
import com.aegira.loan.user.entity.Role;
import com.aegira.loan.user.entity.User;
import com.aegira.loan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final UserRepository userRepository;

    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Unauthenticated");
        }
        return userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public boolean hasRole(Role role) {
        return currentUser().getRole() == role;
    }
}

package com.aegira.loan.common.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {
    private final IdempotencyService idempotencyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (!handlerMethod.hasMethodAnnotation(RequireIdempotency.class)) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication == null ? "anonymous" : authentication.getName();
        String endpoint = request.getMethod() + ":" + request.getRequestURI();
        idempotencyService.checkAndStore(userId, endpoint, request.getHeader("Idempotency-Key"));
        return true;
    }
}

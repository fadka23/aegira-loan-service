package com.aegira.loan.common.idempotency;

import com.aegira.loan.approval.controller.ApprovalController;
import com.aegira.loan.approval.service.ApprovalService;
import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.exception.ConflictException;
import com.aegira.loan.loanapplication.controller.LoanApplicationController;
import com.aegira.loan.loanapplication.service.LoanApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IdempotencyInterceptorTest {
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingIdempotencyKeyReturnsBadRequest() throws Exception {
        IdempotencyService service = mock(IdempotencyService.class);
        doThrow(new BadRequestException("Idempotency-Key header is required"))
                .when(service).checkAndStore(eq("user-1"), eq("POST:/api/v1/loan-applications/" + UUID_ZERO + "/submit"), eq(null));
        IdempotencyInterceptor interceptor = new IdempotencyInterceptor(service);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-1", null));

        assertThrows(BadRequestException.class, () -> interceptor.preHandle(
                request(null),
                new MockHttpServletResponse(),
                submitHandler()
        ));
    }

    @Test
    void duplicateIdempotencyKeyReturnsConflict() throws Exception {
        IdempotencyService service = mock(IdempotencyService.class);
        doThrow(new ConflictException("Duplicate request detected"))
                .when(service).checkAndStore(eq("user-1"), eq("POST:/api/v1/loan-applications/" + UUID_ZERO + "/submit"), eq("same-key"));
        IdempotencyInterceptor interceptor = new IdempotencyInterceptor(service);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-1", null));

        assertThrows(ConflictException.class, () -> interceptor.preHandle(
                request("same-key"),
                new MockHttpServletResponse(),
                submitHandler()
        ));
    }

    @Test
    void submitEndpointRequiresIdempotencyKey() throws Exception {
        Method method = LoanApplicationController.class.getMethod("submit", UUID.class);
        assertTrue(method.isAnnotationPresent(RequireIdempotency.class));
    }

    @Test
    void riskApprovalEndpointRequiresIdempotencyKey() throws Exception {
        Method method = ApprovalController.class.getMethod("riskApprove", UUID.class, com.aegira.loan.approval.dto.ApprovalRequest.class);
        assertTrue(method.isAnnotationPresent(RequireIdempotency.class));
    }

    @Test
    void validKeyIsStoredWithUserAndEndpoint() throws Exception {
        IdempotencyService service = mock(IdempotencyService.class);
        IdempotencyInterceptor interceptor = new IdempotencyInterceptor(service);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user-1", null));

        interceptor.preHandle(request("new-key"), new MockHttpServletResponse(), submitHandler());

        verify(service).checkAndStore("user-1", "POST:/api/v1/loan-applications/" + UUID_ZERO + "/submit", "new-key");
    }

    private HandlerMethod submitHandler() throws NoSuchMethodException {
        LoanApplicationController controller = new LoanApplicationController(
                mock(LoanApplicationService.class),
                mock(com.aegira.loan.calculation.service.LoanCalculationService.class),
                mock(com.aegira.loan.eligibility.service.EligibilityService.class)
        );
        return new HandlerMethod(controller, LoanApplicationController.class.getMethod("submit", UUID.class));
    }

    private MockHttpServletRequest request(String idempotencyKey) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/loan-applications/" + UUID_ZERO + "/submit");
        if (idempotencyKey != null) {
            request.addHeader("Idempotency-Key", idempotencyKey);
        }
        return request;
    }

    private static final String UUID_ZERO = "00000000-0000-0000-0000-000000000001";
}

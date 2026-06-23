package com.aegira.loan.common.exception;

import com.aegira.loan.common.dto.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldReturnCorrelationIdForValidationError() throws Exception {
        MDC.put("correlation_id", "REQ-001");
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "amount", "must be positive"));
        MethodParameter parameter = new MethodParameter(GlobalExceptionHandlerTest.class.getDeclaredMethod("validationTarget", String.class), 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.validation(exception);

        assertEquals("REQ-001", response.getBody().getCorrelationId());
        assertEquals("BAD_REQUEST", response.getBody().getError());
    }

    @Test
    void shouldReturnCorrelationIdForForbiddenError() {
        MDC.put("correlation_id", "REQ-002");

        ResponseEntity<ApiResponse<Void>> response = handler.forbidden(new ForbiddenException("forbidden"));

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("REQ-002", response.getBody().getCorrelationId());
    }

    @Test
    void shouldReturnCorrelationIdForNotFoundError() {
        MDC.put("correlation_id", "REQ-003");

        ResponseEntity<ApiResponse<Void>> response = handler.notFound(new NotFoundException("not found"));

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("REQ-003", response.getBody().getCorrelationId());
    }

    @Test
    void shouldReturnSafeMessageForUnexpectedError() {
        MDC.put("correlation_id", "REQ-004");

        ResponseEntity<ApiResponse<Void>> response = handler.generic(new IllegalStateException("database secret"));

        assertFalse(response.getBody().isSuccess());
        assertEquals("Unexpected error occurred", response.getBody().getMessage());
        assertEquals("REQ-004", response.getBody().getCorrelationId());
    }

    private static void validationTarget(String amount) {
    }
}

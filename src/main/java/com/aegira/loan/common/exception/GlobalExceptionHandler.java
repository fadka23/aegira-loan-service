package com.aegira.loan.common.exception;

import com.aegira.loan.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", ex.getMessage(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> forbidden(ForbiddenException ex) {
        log.warn("event_name=access_denied correlation_id={} error_code=FORBIDDEN", correlationId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("FORBIDDEN", ex.getMessage(), null));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("CONFLICT", ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::format)
                .collect(Collectors.toList());
        log.warn("event_name=validation_error correlation_id={} error_code=BAD_REQUEST", correlationId());
        return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", "Validation failed", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> generic(Exception ex) {
        log.error("event_name=unexpected_error correlation_id={} error_code=INTERNAL_SERVER_ERROR", correlationId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "Unexpected error occurred", null));
    }

    private String format(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    private String correlationId() {
        return MDC.get("correlation_id");
    }
}

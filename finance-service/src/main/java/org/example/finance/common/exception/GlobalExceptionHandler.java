package org.example.finance.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> business(BusinessException ex, HttpServletRequest r) {
        return response(ex.getStatus(), ex.getCode(), ex.getMessage(), r, List.of());
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiError> state(IllegalStateException ex, HttpServletRequest r) {
        return response(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", ex.getMessage(), r, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> argument(IllegalArgumentException ex, HttpServletRequest r) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), r, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> authentication(AuthenticationException ex, HttpServletRequest r) {
        return response(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", ex.getMessage(), r, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(AccessDeniedException ex, HttpServletRequest r) {
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), r, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex, HttpServletRequest r) {
        return response(HttpStatus.CONFLICT, "DATA_CONFLICT", "Operation violates a data constraint", r, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest r) {
        var fields = ex.getBindingResult().getFieldErrors().stream().map(e -> new ApiError.FieldError(e.getField(), e.getDefaultMessage())).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", r, fields);
    }

    private ResponseEntity<ApiError> response(HttpStatus s, String code, String message, HttpServletRequest r, List<ApiError.FieldError> f) {
        return ResponseEntity.status(s).body(new ApiError(Instant.now(), s.value(), code, message, r.getRequestURI(), f));
    }
}

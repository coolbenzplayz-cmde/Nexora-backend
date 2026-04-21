package org.example.nexora.common;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler providing consistent error responses across all endpoints.
 * Handles all application exceptions and returns standardized error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== Generic Exception Handlers ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("Unhandled exception: ", ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // ==================== Validation Exception Handlers ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.badRequest("Validation failed");
        response.setData(errors);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (existing, replacement) -> existing
                ));

        logger.warn("Constraint violation: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.badRequest("Validation failed");
        response.setData(errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        logger.warn("HTTP message not readable: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed request body",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Malformed request body");
        response.setData(error);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    // ==================== Resource Exception Handlers ====================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        logger.warn("Entity not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Resource not found");
        response.setData(error);
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex, WebRequest request) {
        logger.warn("Empty result data access: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Resource not found");
        response.setData(error);
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ==================== Security Exception Handlers ====================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Authentication failed");
        response.setData(error);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        logger.warn("Bad credentials: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid credentials",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Invalid credentials");
        response.setData(error);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access denied",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Access denied");
        response.setData(error);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ==================== Data Exception Handlers ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        logger.error("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Data integrity violation",
                ex.getMostSpecificCause().getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Data integrity violation");
        response.setData(error);
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ==================== HTTP Method Exception Handlers ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        logger.warn("Method not supported: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method not allowed",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Method not allowed");
        response.setData(error);
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        logger.warn("Media type not supported: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Media type not supported",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Media type not supported");
        response.setData(error);
        response.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        logger.warn("Type mismatch: {}", ex.getMessage());

        String message = String.format("Parameter '%s' with value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Type mismatch",
                message,
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Invalid parameter");
        response.setData(error);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {
        logger.warn("Max upload size exceeded: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "File too large",
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("File too large");
        response.setData(error);
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    // ==================== Custom Business Exception Handler ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        logger.warn("Business exception: {} - {}", ex.getCode(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                ex.getHttpStatus().value(),
                ex.getCode(),
                ex.getMessage(),
                request.getDescription(false)
        );

        ApiResponse<ErrorResponse> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(error);
        response.setStatus(ex.getHttpStatus().value());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    // ==================== Error Response DTO ====================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private String path;
        private LocalDateTime timestamp;

        public ErrorResponse(int status, String error, String message, String path) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = LocalDateTime.now();
        }
    }
}
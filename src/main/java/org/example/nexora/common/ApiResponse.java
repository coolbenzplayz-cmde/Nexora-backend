package org.example.nexora.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard API response wrapper for all API endpoints.
 * Provides consistent response format across the application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, Object> metadata;

    // HTTP Status code
    private int status;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Operation completed successfully");
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return response;
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setStatus(status.value());
        return response;
    }

    public static <T> ApiResponse<T> created(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Resource created successfully");
        response.setStatus(HttpStatus.CREATED.value());
        return response;
    }

    public static <T> ApiResponse<T> notFound(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return response;
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return response;
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return response;
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return response;
    }

    public ApiResponse<T> withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }

    public ApiResponse<T> withPath(String path) {
        this.setPath(path);
        return this;
    }

    public ResponseEntity<ApiResponse<T>> toResponseEntity() {
        return ResponseEntity
                .status(this.status)
                .body(this);
    }

    public ResponseEntity<ApiResponse<T>> toResponseEntity(HttpStatus status) {
        this.setStatus(status.value());
        return ResponseEntity
                .status(status)
                .body(this);
    }
}
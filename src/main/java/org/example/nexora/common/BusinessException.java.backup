package org.example.nexora.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * Custom business exception for application-specific errors.
 * Provides proper HTTP status code mapping for different business errors.
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;
    private final HttpStatus httpStatus;
    private String menuItemNotFound;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String code, String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String menuItemNotFound) {
        this.menuItemNotFound = menuItemNotFound;
        httpStatus = null;
        code = "";
    }

    // Predefined business exceptions

    public static BusinessException notFound(String resource) {
        return new BusinessException("NOT_FOUND", resource + " not found", HttpStatus.NOT_FOUND);
    }

    public static BusinessException alreadyExists(String resource) {
        return new BusinessException("ALREADY_EXISTS", resource + " already exists", HttpStatus.CONFLICT);
    }

    public static BusinessException invalidInput(String message) {
        return new BusinessException("INVALID_INPUT", message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public static BusinessException internalError(String message) {
        return new BusinessException("INTERNAL_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static BusinessException serviceUnavailable(String message) {
        return new BusinessException("SERVICE_UNAVAILABLE", message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public String getCode() {
        return code;
    }
}
package org.example.nexora.auth;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Auth Service Controller - Handles authentication endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthServiceController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final MfaService mfaService;
    private final AuditService auditService;

    /**
     * User registration
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        try {
            // Check if user already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.failure("Email already registered"));
            }

            // Create user
            UserDetails userDetails = userService.createUser(request);
            
            // Generate JWT token
            String token = jwtTokenService.generateToken(userDetails);
            
            // Log registration
            auditService.logRegistration(userDetails.getUserId(), request.getEmail());

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setToken(token);
            response.setUserId(userDetails.getUserId());
            response.setEmail(userDetails.getEmail());
            response.setRole(userDetails.getRole());
            response.setExpiresAt(jwtTokenService.getExpirationTime(token));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.badRequest()
                    .body(AuthResponse.failure("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Check MFA if enabled
            if (userDetails.isMfaEnabled()) {
                if (request.getMfaCode() == null) {
                    return ResponseEntity.ok()
                            .body(AuthResponse.mfaRequired("MFA code required"));
                }
                
                if (!mfaService.verifyCode(userDetails.getUserId(), request.getMfaCode())) {
                    return ResponseEntity.badRequest()
                            .body(AuthResponse.failure("Invalid MFA code"));
                }
            }
            
            // Generate JWT token
            String token = jwtTokenService.generateToken(userDetails);
            
            // Update last login
            userService.updateLastLogin(userDetails.getUserId());
            
            // Log successful login
            auditService.logLogin(userDetails.getUserId(), request.getEmail());

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setToken(token);
            response.setUserId(userDetails.getUserId());
            response.setEmail(userDetails.getEmail());
            response.setRole(userDetails.getRole());
            response.setExpiresAt(jwtTokenService.getExpirationTime(token));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest()
                    .body(AuthResponse.failure("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Refreshing token for user");

        try {
            // Validate refresh token
            String refreshToken = request.getRefreshToken();
            if (!jwtTokenService.validateRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.failure("Invalid refresh token"));
            }

            // Get user from token
            Long userId = jwtTokenService.getUserIdFromRefreshToken(refreshToken);
            UserDetails userDetails = userService.getUserById(userId);
            
            // Generate new JWT token
            String newToken = jwtTokenService.generateToken(userDetails);

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setToken(newToken);
            response.setUserId(userDetails.getUserId());
            response.setEmail(userDetails.getEmail());
            response.setRole(userDetails.getRole());
            response.setExpiresAt(jwtTokenService.getExpirationTime(newToken));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(AuthResponse.failure("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Validate JWT token
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(@RequestBody ValidateTokenRequest request) {
        log.debug("Validating token");

        try {
            String token = request.getToken();
            
            if (!jwtTokenService.validateToken(token)) {
                return ResponseEntity.ok()
                        .body(ValidationResponse.invalid("Token is invalid"));
            }

            Long userId = jwtTokenService.getUserIdFromToken(token);
            UserDetails userDetails = userService.getUserById(userId);

            ValidationResponse response = new ValidationResponse();
            response.setValid(true);
            response.setUserId(userDetails.getUserId());
            response.setEmail(userDetails.getEmail());
            response.setRole(userDetails.getRole());
            response.setPermissions(userDetails.getPermissions());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.ok()
                    .body(ValidationResponse.invalid("Token validation failed"));
        }
    }

    /**
     * Enable MFA
     */
    @PostMapping("/mfa/enable")
    public ResponseEntity<MfaResponse> enableMfa(@AuthenticationPrincipal Long userId) {
        log.info("Enabling MFA for user: {}", userId);

        try {
            // Generate MFA secret
            String secret = mfaService.generateSecret(userId);
            String qrCode = mfaService.generateQrCode(userId, secret);
            
            // Update user
            userService.enableMfa(userId, secret);

            MfaResponse response = new MfaResponse();
            response.setSuccess(true);
            response.setSecret(secret);
            response.setQrCode(qrCode);
            response.setBackupCodes(mfaService.generateBackupCodes(userId));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("MFA enable failed", e);
            return ResponseEntity.badRequest()
                    .body(MfaResponse.failure("MFA enable failed: " + e.getMessage()));
        }
    }

    /**
     * Verify MFA code
     */
    @PostMapping("/mfa/verify")
    public ResponseEntity<MfaVerifyResponse> verifyMfa(
            @AuthenticationPrincipal Long userId,
            @RequestBody MfaVerifyRequest request) {
        log.info("Verifying MFA code for user: {}", userId);

        try {
            boolean isValid = mfaService.verifyCode(userId, request.getCode());

            MfaVerifyResponse response = new MfaVerifyResponse();
            response.setSuccess(true);
            response.setValid(isValid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("MFA verification failed", e);
            return ResponseEntity.badRequest()
                    .body(MfaVerifyResponse.failure("MFA verification failed: " + e.getMessage()));
        }
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@AuthenticationPrincipal Long userId) {
        log.info("Logging out user: {}", userId);

        try {
            // Invalidate tokens
            jwtTokenService.invalidateUserTokens(userId);
            
            // Log logout
            auditService.logLogout(userId);

            LogoutResponse response = new LogoutResponse();
            response.setSuccess(true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest()
                    .body(LogoutResponse.failure("Logout failed: " + e.getMessage()));
        }
    }

    // Request classes
    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phone;
        private String dateOfBirth;
        private String gender;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
        private String mfaCode;
    }

    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

    @Data
    public static class ValidateTokenRequest {
        private String token;
    }

    @Data
    public static class MfaVerifyRequest {
        private String code;
    }

    // Response classes
    @Data
    public static class AuthResponse {
        private boolean success;
        private String token;
        private Long userId;
        private String email;
        private String role;
        private java.time.LocalDateTime expiresAt;
        private String error;
        private boolean mfaRequired;

        public static AuthResponse failure(String error) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }

        public static AuthResponse mfaRequired(String message) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMfaRequired(true);
            response.setError(message);
            return response;
        }
    }

    @Data
    public static class ValidationResponse {
        private boolean valid;
        private Long userId;
        private String email;
        private String role;
        private java.util.List<String> permissions;
        private String error;

        public static ValidationResponse invalid(String error) {
            ValidationResponse response = new ValidationResponse();
            response.setValid(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class MfaResponse {
        private boolean success;
        private String secret;
        private String qrCode;
        private java.util.List<String> backupCodes;
        private String error;

        public static MfaResponse failure(String error) {
            MfaResponse response = new MfaResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class MfaVerifyResponse {
        private boolean success;
        private boolean valid;
        private String error;

        public static MfaVerifyResponse failure(String error) {
            MfaVerifyResponse response = new MfaVerifyResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class LogoutResponse {
        private boolean success;
        private String error;

        public static LogoutResponse failure(String error) {
            LogoutResponse response = new LogoutResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }
}

// Service placeholders
class JwtTokenService {
    public String generateToken(UserDetails userDetails) { return "jwt-token"; }
    public boolean validateToken(String token) { return true; }
    public Long getUserIdFromToken(String token) { return 1L; }
    public Long getUserIdFromRefreshToken(String token) { return 1L; }
    public boolean validateRefreshToken(String token) { return true; }
    public java.time.LocalDateTime getExpirationTime(String token) { return java.time.LocalDateTime.now().plusHours(1); }
    public void invalidateUserTokens(Long userId) {}
}

class UserService {
    public boolean existsByEmail(String email) { return false; }
    public UserDetails createUser(RegisterRequest request) { return new UserDetails(); }
    public UserDetails getUserById(Long userId) { return new UserDetails(); }
    public void updateLastLogin(Long userId) {}
    public void enableMfa(Long userId, String secret) {}
}

class MfaService {
    public String generateSecret(Long userId) { return "mfa-secret"; }
    public String generateQrCode(Long userId, String secret) { return "qr-code"; }
    public java.util.List<String> generateBackupCodes(Long userId) { return java.util.Arrays.asList("123456", "789012"); }
    public boolean verifyCode(Long userId, String code) { return true; }
}

class AuditService {
    public void logRegistration(Long userId, String email) {}
    public void logLogin(Long userId, String email) {}
    public void logLogout(Long userId) {}
}

class UserDetails {
    private Long userId;
    private String email;
    private String role;
    private java.util.List<String> permissions;
    private boolean mfaEnabled;

    // Getters and setters
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public java.util.List<String> getPermissions() { return permissions; }
    public boolean isMfaEnabled() { return mfaEnabled; }
}

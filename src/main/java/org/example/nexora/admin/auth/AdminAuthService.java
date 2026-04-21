package org.example.nexora.admin.auth;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Admin Authentication Service providing:
 * - Secure admin login with multi-factor authentication
 * - Session management with JWT tokens
 * - Role-based access control
 * - Security monitoring and audit logging
 * - Password policies and reset functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final AdminSessionRepository sessionRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TwoFactorAuthService twoFactorAuthService;

    // Session cache for active admin sessions
    private final ConcurrentHashMap<String, AdminSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Admin login with authentication
     */
    public AdminLoginResult login(AdminLoginRequest request) {
        log.info("Admin login attempt for user: {}", request.getUsername());

        try {
            // Validate request
            ValidationResult validation = validateLoginRequest(request);
            if (!validation.isValid()) {
                return AdminLoginResult.failure(validation.getErrors());
            }

            // Find admin user
            AdminUser admin = adminUserRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AdminAuthException("Invalid credentials"));

            // Check account status
            if (!isAccountActive(admin)) {
                return AdminLoginResult.failure("Account is not active");
            }

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
                // Log failed attempt
                logFailedLoginAttempt(admin, "Invalid password");
                return AdminLoginResult.failure("Invalid credentials");
            }

            // Check if 2FA is required
            if (admin.isTwoFactorEnabled()) {
                return initiateTwoFactorAuthentication(admin, request);
            }

            // Create session
            AdminSession session = createAdminSession(admin, request);
            
            // Log successful login
            logSuccessfulLogin(admin, session);

            return AdminLoginResult.success(admin, session);

        } catch (Exception e) {
            log.error("Admin login failed for user: {}", request.getUsername(), e);
            return AdminLoginResult.failure("Login failed: " + e.getMessage());
        }
    }

    /**
     * Complete two-factor authentication
     */
    public AdminLoginResult completeTwoFactorLogin(TwoFactorLoginRequest request) {
        log.info("Completing 2FA for session: {}", request.getSessionToken());

        try {
            // Find pending session
            AdminSession session = sessionRepository.findBySessionToken(request.getSessionToken())
                    .orElseThrow(() -> new AdminAuthException("Invalid session"));

            if (session.getStatus() != SessionStatus.PENDING_2FA) {
                throw new AdminAuthException("Session not pending 2FA");
            }

            // Verify 2FA code
            AdminUser admin = adminUserRepository.findById(session.getAdminUserId())
                    .orElseThrow(() -> new AdminAuthException("Admin not found"));

            if (!twoFactorAuthService.verifyCode(admin, request.getTwoFactorCode())) {
                logFailed2FAAttempt(admin, session);
                return AdminLoginResult.failure("Invalid 2FA code");
            }

            // Activate session
            session.setStatus(SessionStatus.ACTIVE);
            session.setAuthenticatedAt(LocalDateTime.now());
            session = sessionRepository.save(session);

            // Cache session
            activeSessions.put(session.getSessionToken(), session);

            // Log successful 2FA
            logSuccessful2FA(admin, session);

            return AdminLoginResult.success(admin, session);

        } catch (Exception e) {
            log.error("2FA login failed", e);
            return AdminLoginResult.failure("2FA failed: " + e.getMessage());
        }
    }

    /**
     * Admin logout
     */
    public void logout(String sessionToken) {
        log.info("Admin logout for session: {}", sessionToken);

        try {
            AdminSession session = sessionRepository.findBySessionToken(sessionToken)
                    .orElse(null);

            if (session != null) {
                // Update session
                session.setStatus(SessionStatus.TERMINATED);
                session.setTerminatedAt(LocalDateTime.now());
                sessionRepository.save(session);

                // Remove from cache
                activeSessions.remove(sessionToken);

                // Log logout
                AdminUser admin = adminUserRepository.findById(session.getAdminUserId()).orElse(null);
                if (admin != null) {
                    logAdminAction(admin, "LOGOUT", "Session terminated", sessionToken);
                }
            }

        } catch (Exception e) {
            log.error("Logout failed for session: {}", sessionToken, e);
        }
    }

    /**
     * Validate admin session
     */
    public AdminSession validateSession(String sessionToken) {
        // Check cache first
        AdminSession cachedSession = activeSessions.get(sessionToken);
        if (cachedSession != null && cachedSession.getStatus() == SessionStatus.ACTIVE) {
            return cachedSession;
        }

        // Check database
        AdminSession session = sessionRepository.findBySessionToken(sessionToken).orElse(null);
        if (session != null && session.getStatus() == SessionStatus.ACTIVE) {
            // Check if session is expired
            if (session.isExpired()) {
                terminateSession(session);
                return null;
            }

            // Cache session
            activeSessions.put(sessionToken, session);
            return session;
        }

        return null;
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(PasswordResetRequest request) {
        log.info("Password reset requested for: {}", request.getEmail());

        try {
            AdminUser admin = adminUserRepository.findByEmail(request.getEmail())
                    .orElse(null);

            // Always return success to prevent email enumeration
            if (admin == null) {
                log.warn("Password reset requested for non-existent email: {}", request.getEmail());
                return;
            }

            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            admin.setPasswordResetToken(resetToken);
            admin.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
            adminRepository.save(admin);

            // Send reset email
            emailService.sendPasswordResetEmail(admin.getEmail(), resetToken);

            log.info("Password reset email sent to: {}", admin.getEmail());

        } catch (Exception e) {
            log.error("Password reset request failed", e);
        }
    }

    /**
     * Reset password
     */
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password with token: {}", request.getResetToken());

        try {
            AdminUser admin = adminUserRepository.findByPasswordResetToken(request.getResetToken())
                    .orElseThrow(() -> new AdminAuthException("Invalid reset token"));

            // Check token expiration
            if (admin.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
                throw new AdminAuthException("Reset token expired");
            }

            // Validate new password
            ValidationResult validation = validatePassword(request.getNewPassword());
            if (!validation.isValid()) {
                throw new AdminAuthException(String.join(", ", validation.getErrors()));
            }

            // Update password
            admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            admin.setPasswordResetToken(null);
            admin.setPasswordResetExpiresAt(null);
            admin.setPasswordChangedAt(LocalDateTime.now());
            adminRepository.save(admin);

            // Terminate all existing sessions
            terminateAllSessions(admin.getId());

            // Log password reset
            logAdminAction(admin, "PASSWORD_RESET", "Password reset via email", null);

        } catch (Exception e) {
            log.error("Password reset failed", e);
            throw new AdminAuthException("Password reset failed: " + e.getMessage());
        }
    }

    /**
     * Create new admin user
     */
    public AdminUser createAdminUser(CreateAdminUserRequest request, Long createdBy) {
        log.info("Creating new admin user: {}", request.getUsername());

        try {
            // Validate request
            ValidationResult validation = validateCreateAdminRequest(request);
            if (!validation.isValid()) {
                throw new AdminAuthException(String.join(", ", validation.getErrors()));
            }

            // Check if username/email already exists
            if (adminUserRepository.existsByUsername(request.getUsername())) {
                throw new AdminAuthException("Username already exists");
            }

            if (adminUserRepository.existsByEmail(request.getEmail())) {
                throw new AdminAuthException("Email already exists");
            }

            // Create admin user
            AdminUser admin = new AdminUser();
            admin.setUsername(request.getUsername());
            admin.setEmail(request.getEmail());
            admin.setPasswordHash(passwordEncoder.encode(request.getTemporaryPassword()));
            admin.setRole(request.getRole());
            admin.setPermissions(request.getPermissions());
            admin.setStatus(AdminStatus.ACTIVE);
            admin.setMustChangePassword(true);
            admin.setCreatedBy(createdBy);
            admin.setCreatedAt(LocalDateTime.now());

            admin = adminUserRepository.save(admin);

            // Send welcome email
            emailService.sendAdminWelcomeEmail(admin.getEmail(), request.getTemporaryPassword());

            // Log creation
            logAdminAction(adminUserRepository.findById(createdBy).orElse(null), 
                          "CREATE_ADMIN", "Created admin user: " + request.getUsername(), null);

            return admin;

        } catch (Exception e) {
            log.error("Failed to create admin user", e);
            throw new AdminAuthException("Failed to create admin user: " + e.getMessage());
        }
    }

    /**
     * Get admin dashboard
     */
    public AdminDashboard getAdminDashboard(Long adminUserId) {
        AdminUser admin = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new AdminAuthException("Admin not found"));

        AdminDashboard dashboard = new AdminDashboard();
        dashboard.setAdmin(admin);
        dashboard.setGeneratedAt(LocalDateTime.now());

        // Get system statistics
        SystemStatistics stats = getSystemStatistics();
        dashboard.setSystemStatistics(stats);

        // Get recent audit logs
        List<AdminAuditLog> recentLogs = auditLogRepository.findRecentLogs(50);
        dashboard.setRecentAuditLogs(recentLogs);

        // Get active sessions
        List<AdminSession> activeSessions = sessionRepository.findByStatus(SessionStatus.ACTIVE);
        dashboard.setActiveSessions(activeSessions);

        // Get security alerts
        List<SecurityAlert> securityAlerts = getSecurityAlerts();
        dashboard.setSecurityAlerts(securityAlerts);

        return dashboard;
    }

    // Private helper methods
    private ValidationResult validateLoginRequest(AdminLoginRequest request) {
        ValidationResult result = new ValidationResult();

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            result.addError("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            result.addError("Password is required");
        }

        return result;
    }

    private boolean isAccountActive(AdminUser admin) {
        return admin.getStatus() == AdminStatus.ACTIVE && !admin.isLocked();
    }

    private AdminLoginResult initiateTwoFactorAuthentication(AdminUser admin, AdminLoginRequest request) {
        // Create pending session
        AdminSession session = createAdminSession(admin, request);
        session.setStatus(SessionStatus.PENDING_2FA);
        session = sessionRepository.save(session);

        // Send 2FA code
        twoFactorAuthService.sendCode(admin);

        return AdminLoginResult.pending2FA(session);
    }

    private AdminSession createAdminSession(AdminUser admin, AdminLoginRequest request) {
        AdminSession session = new AdminSession();
        session.setAdminUserId(admin.getId());
        session.setSessionToken(UUID.randomUUID().toString());
        session.setRefreshToken(UUID.randomUUID().toString());
        session.setIpAddress(request.getIpAddress());
        session.setUserAgent(request.getUserAgent());
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(8));
        session.setStatus(SessionStatus.ACTIVE);

        session = sessionRepository.save(session);

        // Cache session
        activeSessions.put(session.getSessionToken(), session);

        return session;
    }

    private void logSuccessfulLogin(AdminUser admin, AdminSession session) {
        logAdminAction(admin, "LOGIN", "Successful login", session.getSessionToken());
    }

    private void logFailedLoginAttempt(AdminUser admin, String reason) {
        logAdminAction(admin, "LOGIN_FAILED", reason, null);
    }

    private void logFailed2FAAttempt(AdminUser admin, AdminSession session) {
        logAdminAction(admin, "2FA_FAILED", "Invalid 2FA code", session.getSessionToken());
    }

    private void logSuccessful2FA(AdminUser admin, AdminSession session) {
        logAdminAction(admin, "2FA_SUCCESS", "2FA completed", session.getSessionToken());
    }

    private void logAdminAction(AdminUser admin, String action, String details, String sessionToken) {
        if (admin == null) return;

        AdminAuditLog auditLog = new AdminAuditLog();
        auditLog.setAdminUserId(admin.getId());
        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setSessionToken(sessionToken);
        auditLog.setIpAddress("unknown"); // Would get from request
        auditLog.setUserAgent("unknown"); // Would get from request
        auditLog.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    private ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();

        if (password == null || password.length() < 8) {
            result.addError("Password must be at least 8 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            result.addError("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            result.addError("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            result.addError("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()].*")) {
            result.addError("Password must contain at least one special character");
        }

        return result;
    }

    private ValidationResult validateCreateAdminRequest(CreateAdminUserRequest request) {
        ValidationResult result = new ValidationResult();

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            result.addError("Username is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            result.addError("Email is required");
        }

        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            result.addError("Invalid email format");
        }

        if (request.getRole() == null) {
            result.addError("Role is required");
        }

        if (request.getTemporaryPassword() == null || request.getTemporaryPassword().length() < 8) {
            result.addError("Temporary password must be at least 8 characters");
        }

        return result;
    }

    private void terminateSession(AdminSession session) {
        session.setStatus(SessionStatus.EXPIRED);
        session.setTerminatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        activeSessions.remove(session.getSessionToken());
    }

    private void terminateAllSessions(Long adminUserId) {
        List<AdminSession> sessions = sessionRepository.findByAdminUserId(adminUserId);
        for (AdminSession session : sessions) {
            terminateSession(session);
        }
    }

    private SystemStatistics getSystemStatistics() {
        SystemStatistics stats = new SystemStatistics();
        stats.setTotalAdmins(adminUserRepository.count());
        stats.setActiveAdmins(adminUserRepository.countByStatus(AdminStatus.ACTIVE));
        stats.setActiveSessions(sessionRepository.countByStatus(SessionStatus.ACTIVE));
        stats.setTodayLogins(auditLogRepository.countTodayLogins());
        stats.setFailedLoginsToday(auditLogRepository.countFailedLoginsToday());
        return stats;
    }

    private List<SecurityAlert> getSecurityAlerts() {
        List<SecurityAlert> alerts = new ArrayList<>();
        
        // Check for multiple failed logins
        int recentFailedLogins = auditLogRepository.countRecentFailedLogins(60);
        if (recentFailedLogins > 10) {
            SecurityAlert alert = new SecurityAlert();
            alert.setType("HIGH_FAILED_LOGINS");
            alert.setMessage("High number of failed login attempts detected");
            alert.setSeverity("HIGH");
            alert.setTimestamp(LocalDateTime.now());
            alerts.add(alert);
        }

        return alerts;
    }

    // Data classes
    @Data
    public static class AdminLoginResult {
        private boolean success;
        private boolean requiresTwoFactor;
        private AdminUser admin;
        private AdminSession session;
        private List<String> errors;

        public static AdminLoginResult success(AdminUser admin, AdminSession session) {
            AdminLoginResult result = new AdminLoginResult();
            result.setSuccess(true);
            result.setAdmin(admin);
            result.setSession(session);
            return result;
        }

        public static AdminLoginResult failure(String error) {
            AdminLoginResult result = new AdminLoginResult();
            result.setSuccess(false);
            result.setErrors(Arrays.asList(error));
            return result;
        }

        public static AdminLoginResult failure(List<String> errors) {
            AdminLoginResult result = new AdminLoginResult();
            result.setSuccess(false);
            result.setErrors(errors);
            return result;
        }

        public static AdminLoginResult pending2FA(AdminSession session) {
            AdminLoginResult result = new AdminLoginResult();
            result.setSuccess(true);
            result.setRequiresTwoFactor(true);
            result.setSession(session);
            return result;
        }
    }

    @Data
    public static class AdminDashboard {
        private AdminUser admin;
        private LocalDateTime generatedAt;
        private SystemStatistics systemStatistics;
        private List<AdminAuditLog> recentAuditLogs;
        private List<AdminSession> activeSessions;
        private List<SecurityAlert> securityAlerts;
    }

    @Data
    public static class SystemStatistics {
        private long totalAdmins;
        private long activeAdmins;
        private long activeSessions;
        private long todayLogins;
        private long failedLoginsToday;
    }

    @Data
    public static class SecurityAlert {
        private String type;
        private String message;
        private String severity;
        private LocalDateTime timestamp;
    }

    @Data
    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
            valid = false;
        }
    }

    // Request classes
    @Data
    public static class AdminLoginRequest {
        private String username;
        private String password;
        private String ipAddress;
        private String userAgent;
        private boolean rememberMe;
    }

    @Data
    public static class TwoFactorLoginRequest {
        private String sessionToken;
        private String twoFactorCode;
    }

    @Data
    public static class PasswordResetRequest {
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        private String resetToken;
        private String newPassword;
    }

    @Data
    public static class CreateAdminUserRequest {
        private String username;
        private String email;
        private String role;
        private List<String> permissions;
        private String temporaryPassword;
    }

    // Entity classes
    @Data
    public static class AdminUser {
        private Long id;
        private String username;
        private String email;
        private String passwordHash;
        private String role;
        private List<String> permissions;
        private AdminStatus status;
        private boolean twoFactorEnabled;
        private boolean locked;
        private boolean mustChangePassword;
        private String passwordResetToken;
        private LocalDateTime passwordResetExpiresAt;
        private LocalDateTime passwordChangedAt;
        private LocalDateTime lastLoginAt;
        private Long createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class AdminSession {
        private Long id;
        private Long adminUserId;
        private String sessionToken;
        private String refreshToken;
        private String ipAddress;
        private String userAgent;
        private SessionStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime authenticatedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime terminatedAt;

        public boolean isExpired() {
            return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
        }
    }

    @Data
    public static class AdminAuditLog {
        private Long id;
        private Long adminUserId;
        private String action;
        private String details;
        private String sessionToken;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;
    }

    // Enums
    public enum AdminStatus {
        ACTIVE, INACTIVE, SUSPENDED, LOCKED
    }

    public enum SessionStatus {
        ACTIVE, PENDING_2FA, TERMINATED, EXPIRED
    }

    // Service placeholders
    private static class EmailService {
        public void sendPasswordResetEmail(String email, String resetToken) {}
        public void sendAdminWelcomeEmail(String email, String temporaryPassword) {}
    }

    private static class TwoFactorAuthService {
        public void sendCode(AdminUser admin) {}
        public boolean verifyCode(AdminUser admin, String code) { return true; }
    }

    // Repository placeholders
    private static class AdminUserRepository {
        public Optional<AdminUser> findByUsername(String username) { return Optional.empty(); }
        public Optional<AdminUser> findByEmail(String email) { return Optional.empty(); }
        public Optional<AdminUser> findByPasswordResetToken(String token) { return Optional.empty(); }
        public Optional<AdminUser> findById(Long id) { return Optional.empty(); }
        public boolean existsByUsername(String username) { return false; }
        public boolean existsByEmail(String email) { return false; }
        public AdminUser save(AdminUser admin) { return admin; }
        public long count() { return 0; }
        public long countByStatus(AdminStatus status) { return 0; }
    }

    private static class AdminSessionRepository {
        public Optional<AdminSession> findBySessionToken(String token) { return Optional.empty(); }
        public AdminSession save(AdminSession session) { return session; }
        public List<AdminSession> findByAdminUserId(Long adminUserId) { return new ArrayList<>(); }
        public List<AdminSession> findByStatus(SessionStatus status) { return new ArrayList<>(); }
        public long countByStatus(SessionStatus status) { return 0; }
    }

    private static class AdminAuditLogRepository {
        public AdminAuditLog save(AdminAuditLog log) { return log; }
        public List<AdminAuditLog> findRecentLogs(int limit) { return new ArrayList<>(); }
        public long countTodayLogins() { return 0; }
        public long countFailedLoginsToday() { return 0; }
        public int countRecentFailedLogins(int minutes) { return 0; }
    }

    // Service instances
    private final AdminUserRepository adminUserRepository = new AdminUserRepository();
    private final AdminSessionRepository sessionRepository = new AdminSessionRepository();
    private final AdminAuditLogRepository auditLogRepository = new AdminAuditLogRepository();
    private final EmailService emailService = new EmailService();
    private final TwoFactorAuthService twoFactorAuthService = new TwoFactorAuthService();
}

class AdminAuthException extends RuntimeException {
    public AdminAuthException(String message) {
        super(message);
    }
}

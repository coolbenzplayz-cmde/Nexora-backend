package org.example.nexora.admin.dashboard;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Admin Dashboard Controller providing:
 * - Complete admin interface for service management
 * - Authentication and authorization
 * - Real-time service monitoring
 * - Configuration management
 * - User and system management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminAuthService adminAuthService;
    private final ServiceManagementService serviceManagementService;
    private final AdminDashboardService dashboardService;

    /**
     * Admin login endpoint
     */
    @PostMapping("/auth/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminAuthService.AdminLoginResult result = adminAuthService.login(request);
        
        if (result.isSuccess()) {
            AdminLoginResponse response = new AdminLoginResponse();
            response.setSuccess(true);
            response.setAdmin(result.getAdmin());
            response.setSessionToken(result.getSession().getSessionToken());
            response.setRefreshToken(result.getSession().getRefreshToken());
            response.setExpiresAt(result.getSession().getExpiresAt());
            response.setRequiresTwoFactor(result.isRequiresTwoFactor());
            
            return ResponseEntity.ok(response);
        } else {
            AdminLoginResponse response = new AdminLoginResponse();
            response.setSuccess(false);
            response.setErrors(result.getErrors());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Complete two-factor authentication
     */
    @PostMapping("/auth/2fa/complete")
    public ResponseEntity<AdminLoginResponse> completeTwoFactorLogin(@Valid @RequestBody TwoFactorLoginRequest request) {
        AdminAuthService.AdminLoginResult result = adminAuthService.completeTwoFactorLogin(request);
        
        if (result.isSuccess()) {
            AdminLoginResponse response = new AdminLoginResponse();
            response.setSuccess(true);
            response.setAdmin(result.getAdmin());
            response.setSessionToken(result.getSession().getSessionToken());
            response.setRefreshToken(result.getSession().getRefreshToken());
            response.setExpiresAt(result.getSession().getExpiresAt());
            
            return ResponseEntity.ok(response);
        } else {
            AdminLoginResponse response = new AdminLoginResponse();
            response.setSuccess(false);
            response.setErrors(result.getErrors());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin logout
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-Admin-Token") String sessionToken) {
        adminAuthService.logout(sessionToken);
        return ResponseEntity.ok().build();
    }

    /**
     * Get admin dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboard(@RequestHeader("X-Admin-Token") String sessionToken) {
        // Validate session
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        AdminAuthService.AdminDashboard dashboard = adminAuthService.getAdminDashboard(session.getAdminUserId());
        
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setAdmin(dashboard.getAdmin());
        response.setSystemStatistics(dashboard.getSystemStatistics());
        response.setRecentAuditLogs(dashboard.getRecentAuditLogs());
        response.setActiveSessions(dashboard.getActiveSessions());
        response.setSecurityAlerts(dashboard.getSecurityAlerts());
        response.setGeneratedAt(dashboard.getGeneratedAt());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all services
     */
    @GetMapping("/services")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceRegistry>> getServices(@RequestParam(required = false) String category,
                                                           @RequestParam(required = false) String status,
                                                           @RequestParam(required = false) String serviceType) {
        ServiceManagementService.ServiceFilter filter = new ServiceManagementService.ServiceFilter();
        filter.setCategory(category);
        filter.setStatus(status != null ? ServiceManagementService.ServiceStatus.valueOf(status) : null);
        filter.setServiceType(serviceType != null ? ServiceManagementService.ServiceType.valueOf(serviceType) : null);

        List<ServiceRegistry> services = serviceManagementService.getServices(filter);
        return ResponseEntity.ok(services);
    }

    /**
     * Register new service
     */
    @PostMapping("/services/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceRegistrationResponse> registerService(@RequestHeader("X-Admin-Token") String sessionToken,
                                                                    @Valid @RequestBody ServiceRegistrationRequest request) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.ServiceRegistrationResult result = serviceManagementService.registerService(request, session.getAdminUserId());
        
        if (result.isSuccess()) {
            ServiceRegistrationResponse response = new ServiceRegistrationResponse();
            response.setSuccess(true);
            response.setService(result.getService());
            return ResponseEntity.ok(response);
        } else {
            ServiceRegistrationResponse response = new ServiceRegistrationResponse();
            response.setSuccess(false);
            response.setErrors(result.getErrors());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update service
     */
    @PutMapping("/services/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceUpdateResponse> updateService(@RequestHeader("X-Admin-Token") String sessionToken,
                                                            @PathVariable Long serviceId,
                                                            @Valid @RequestBody ServiceUpdateRequest request) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.ServiceUpdateResult result = serviceManagementService.updateService(serviceId, request, session.getAdminUserId());
        
        if (result.isSuccess()) {
            ServiceUpdateResponse response = new ServiceUpdateResponse();
            response.setSuccess(true);
            response.setService(result.getService());
            return ResponseEntity.ok(response);
        } else {
            ServiceUpdateResponse response = new ServiceUpdateResponse();
            response.setSuccess(false);
            response.setErrors(result.getErrors());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Remove service
     */
    @DeleteMapping("/services/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceRemovalResponse> removeService(@RequestHeader("X-Admin-Token") String sessionToken,
                                                              @PathVariable Long serviceId) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.ServiceRemovalResult result = serviceManagementService.removeService(serviceId, session.getAdminUserId());
        
        if (result.isSuccess()) {
            ServiceRemovalResponse response = new ServiceRemovalResponse();
            response.setSuccess(true);
            response.setService(result.getService());
            return ResponseEntity.ok(response);
        } else {
            ServiceRemovalResponse response = new ServiceRemovalResponse();
            response.setSuccess(false);
            response.setErrors(result.getErrors());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Deploy service
     */
    @PostMapping("/services/{serviceId}/deploy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDeploymentResponse> deployService(@RequestHeader("X-Admin-Token") String sessionToken,
                                                                 @PathVariable Long serviceId,
                                                                 @Valid @RequestBody ServiceDeploymentRequest request) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.ServiceDeploymentResult result = serviceManagementService.deployService(serviceId, request, session.getAdminUserId());
        
        if (result.isSuccess()) {
            ServiceDeploymentResponse response = new ServiceDeploymentResponse();
            response.setSuccess(true);
            response.setDeployment(result.getDeployment());
            return ResponseEntity.ok(response);
        } else {
            ServiceDeploymentResponse response = new ServiceDeploymentResponse();
            response.setSuccess(false);
            response.setErrors(result.getErrors());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Scale service
     */
    @PostMapping("/services/{serviceId}/scale")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceScalingResponse> scaleService(@RequestHeader("X-Admin-Token") String sessionToken,
                                                            @PathVariable Long serviceId,
                                                            @Valid @RequestBody ServiceScalingRequest request) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.ServiceScalingResult result = serviceManagementService.scaleService(serviceId, request, session.getAdminUserId());
        
        if (result.isSuccess()) {
            ServiceScalingResponse response = new ServiceScalingResponse();
            response.setSuccess(true);
            response.setMessage(result.getMessage());
            return ResponseEntity.ok(response);
        } else {
            ServiceScalingResponse response = new ServiceScalingResponse();
            response.setSuccess(false);
            response.setErrors(result.getErrors());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get service details
     */
    @GetMapping("/services/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDetailResponse> getServiceDetail(@RequestHeader("X-Admin-Token") String sessionToken,
                                                               @PathVariable Long serviceId) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.ServiceDetail detail = serviceManagementService.getServiceDetail(serviceId);
        
        ServiceDetailResponse response = new ServiceDetailResponse();
        response.setService(detail.getService());
        response.setInstances(detail.getInstances());
        response.setConfigurations(detail.getConfigurations());
        response.setRecentMetrics(detail.getRecentMetrics());
        response.setDeployments(detail.getDeployments());
        response.setDependencies(detail.getDependencies());
        response.setDependentServices(detail.getDependentServices());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get service health status
     */
    @GetMapping("/services/{serviceId}/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceHealthStatusResponse> getServiceHealth(@RequestHeader("X-Admin-Token") String sessionToken,
                                                                       @PathVariable Long serviceId) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.ServiceHealthStatus healthStatus = serviceManagementService.getServiceHealthStatus(serviceId);
        
        return ResponseEntity.ok(healthStatus);
    }

    /**
     * Get service metrics
     */
    @GetMapping("/services/{serviceId}/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceMetricsSummary> getServiceMetrics(@RequestHeader("X-Admin-Token") String sessionToken,
                                                                  @PathVariable Long serviceId,
                                                                  @RequestParam String timeRange) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ServiceManagementService.MetricsRequest request = new ServiceManagementService.MetricsRequest();
        request.setTimeRange(timeRange);
        
        ServiceManagementService.ServiceMetricsSummary metrics = serviceManagementService.getServiceMetrics(serviceId, request);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get system overview
     */
    @GetMapping("/system/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemOverviewResponse> getSystemOverview(@RequestHeader("X-Admin-Token") String sessionToken) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        SystemOverviewResponse overview = dashboardService.getSystemOverview();
        return ResponseEntity.ok(overview);
    }

    /**
     * Get user management data
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserManagementResponse> getUserManagement(@RequestHeader("X-Admin-Token") String sessionToken,
                                                                  @RequestParam(required = false) String role,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "50") int size) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        UserManagementResponse response = dashboardService.getUserManagement(role, status, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Create admin user
     */
    @PostMapping("/users/admin/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CreateAdminResponse> createAdminUser(@RequestHeader("X-Admin-Token") String sessionToken,
                                                              @Valid @RequestBody CreateAdminUserRequest request) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        AdminAuthService.AdminUser admin = adminAuthService.createAdminUser(request, session.getAdminUserId());
        
        CreateAdminResponse response = new CreateAdminResponse();
        response.setSuccess(true);
        response.setAdmin(admin);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit logs
     */
    @GetMapping("/audit/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogsResponse> getAuditLogs(@RequestHeader("X-Admin-Token") String sessionToken,
                                                         @RequestParam(required = false) String action,
                                                         @RequestParam(required = false) LocalDateTime startDate,
                                                         @RequestParam(required = false) LocalDateTime endDate,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "100") int size) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        AuditLogsResponse response = dashboardService.getAuditLogs(action, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get configuration management
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfigurationManagementResponse> getConfigurationManagement(@RequestHeader("X-Admin-Token") String sessionToken) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ConfigurationManagementResponse response = dashboardService.getConfigurationManagement();
        return ResponseEntity.ok(response);
    }

    /**
     * Update system configuration
     */
    @PutMapping("/config/{configKey}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ConfigurationUpdateResponse> updateConfiguration(@RequestHeader("X-Admin-Token") String sessionToken,
                                                                          @PathVariable String configKey,
                                                                          @RequestBody Map<String, Object> configValue) {
        AdminAuthService.AdminSession session = adminAuthService.validateSession(sessionToken);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        ConfigurationUpdateResponse response = dashboardService.updateConfiguration(configKey, configValue, session.getAdminUserId());
        return ResponseEntity.ok(response);
    }

    // Response classes
    @Data
    public static class AdminLoginResponse {
        private boolean success;
        private AdminAuthService.AdminUser admin;
        private String sessionToken;
        private String refreshToken;
        private LocalDateTime expiresAt;
        private boolean requiresTwoFactor;
        private List<String> errors;
    }

    @Data
    public static class AdminDashboardResponse {
        private AdminAuthService.AdminUser admin;
        private AdminAuthService.SystemStatistics systemStatistics;
        private List<AdminAuthService.AdminAuditLog> recentAuditLogs;
        private List<AdminAuthService.AdminSession> activeSessions;
        private List<AdminAuthService.SecurityAlert> securityAlerts;
        private LocalDateTime generatedAt;
    }

    @Data
    public static class ServiceRegistrationResponse {
        private boolean success;
        private ServiceRegistry service;
        private List<String> errors;
    }

    @Data
    public static class ServiceUpdateResponse {
        private boolean success;
        private ServiceRegistry service;
        private List<String> errors;
    }

    @Data
    public static class ServiceRemovalResponse {
        private boolean success;
        private ServiceRegistry service;
        private List<String> errors;
    }

    @Data
    public static class ServiceDeploymentResponse {
        private boolean success;
        private ServiceManagementService.ServiceDeployment deployment;
        private List<String> errors;
    }

    @Data
    public static class ServiceScalingResponse {
        private boolean success;
        private String message;
        private List<String> errors;
    }

    @Data
    public static class ServiceDetailResponse {
        private ServiceRegistry service;
        private List<ServiceManagementService.ServiceInstance> instances;
        private List<ServiceManagementService.ServiceConfiguration> configurations;
        private List<ServiceManagementService.ServiceMetrics> recentMetrics;
        private List<ServiceManagementService.ServiceDeployment> deployments;
        private List<ServiceRegistry> dependencies;
        private List<ServiceRegistry> dependentServices;
    }

    @Data
    public static class ServiceHealthStatusResponse extends ServiceManagementService.ServiceHealthStatus {
        // Inherits all fields from ServiceHealthStatus
    }

    @Data
    public static class SystemOverviewResponse {
        private Map<String, Object> systemMetrics;
        private List<Map<String, Object>> serviceStatus;
        private List<Map<String, Object>> recentAlerts;
        private Map<String, Object> performanceMetrics;
        private LocalDateTime generatedAt;
    }

    @Data
    public static class UserManagementResponse {
        private List<Map<String, Object>> users;
        private long totalUsers;
        private long activeUsers;
        private Map<String, Long> usersByRole;
        private int currentPage;
        private int totalPages;
        private long totalElements;
    }

    @Data
    public static class CreateAdminResponse {
        private boolean success;
        private AdminAuthService.AdminUser admin;
        private String message;
    }

    @Data
    public static class AuditLogsResponse {
        private List<AdminAuthService.AdminAuditLog> logs;
        private long totalLogs;
        private int currentPage;
        private int totalPages;
    }

    @Data
    public static class ConfigurationManagementResponse {
        private Map<String, Object> systemConfigurations;
        private Map<String, Object> serviceConfigurations;
        private Map<String, Object> securityConfigurations;
        private List<Map<String, Object>> recentChanges;
    }

    @Data
    public static class ConfigurationUpdateResponse {
        private boolean success;
        private String message;
        private Map<String, Object> updatedConfiguration;
    }

    // Request classes (reusing from services)
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
    public static class ServiceRegistrationRequest {
        private String serviceName;
        private String displayName;
        private String description;
        private ServiceManagementService.ServiceType serviceType;
        private String category;
        private String version;
        private String serviceClass;
        private String endpoint;
        private Integer port;
        private String protocol;
        private String healthCheckUrl;
        private String metricsUrl;
        private List<String> dependencies;
        private Map<String, Object> configuration;
        private Boolean autoScalingEnabled = false;
        private Integer minInstances = 1;
        private Integer maxInstances = 10;
        private Integer desiredInstances = 1;
    }

    @Data
    public static class ServiceUpdateRequest {
        private String displayName;
        private String description;
        private String version;
        private String endpoint;
        private Integer port;
        private String healthCheckUrl;
        private Map<String, Object> configuration;
        private Integer minInstances;
        private Integer maxInstances;
        private Integer desiredInstances;
        private Boolean autoScalingEnabled;
    }

    @Data
    public static class ServiceDeploymentRequest {
        private String version;
        private ServiceManagementService.DeploymentType deploymentType;
        private String environment;
        private Map<String, Object> deploymentConfig;
        private boolean rollbackOnFailure = true;
    }

    @Data
    public static class ServiceScalingRequest {
        private int targetInstances;
        private ServiceManagementService.ScalingReason reason;
        private String comment;
    }

    @Data
    public static class CreateAdminUserRequest {
        private String username;
        private String email;
        private String role;
        private List<String> permissions;
        private String temporaryPassword;
    }
}

package org.example.nexora.admin.dashboard;

import org.example.nexora.admin.AdminAuthService;
import org.example.nexora.admin.service.ServiceManagementService;
import org.example.nexora.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Dashboard Service providing:
 * - System overview and monitoring
 * - User management capabilities
 * - Configuration management
 * - Audit log management
 * - Real-time dashboard data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ServiceManagementService serviceManagementService;
    private final AdminAuthService adminAuthService;
    private final UserRepository userRepository;
    private final SystemConfigurationRepository configRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Get system overview
     */
    public SystemOverviewResponse getSystemOverview() {
        SystemOverviewResponse overview = new SystemOverviewResponse();
        overview.setGeneratedAt(LocalDateTime.now());

        // System metrics
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("totalServices", serviceManagementService.getServices(new ServiceManagementService.ServiceFilter()).size());
        systemMetrics.put("runningServices", getRunningServicesCount());
        systemMetrics.put("totalUsers", userRepository.count());
        systemMetrics.put("activeUsers", getActiveUsersCount());
        systemMetrics.put("systemUptime", getSystemUptime());
        systemMetrics.put("cpuUsage", getCpuUsage());
        systemMetrics.put("memoryUsage", getMemoryUsage());
        systemMetrics.put("diskUsage", getDiskUsage());
        overview.setSystemMetrics(systemMetrics);

        // Service status
        List<Map<String, Object>> serviceStatus = getServiceStatusList();
        overview.setServiceStatus(serviceStatus);

        // Recent alerts
        List<Map<String, Object>> recentAlerts = getRecentAlerts();
        overview.setRecentAlerts(recentAlerts);

        // Performance metrics
        Map<String, Object> performanceMetrics = getPerformanceMetrics();
        overview.setPerformanceMetrics(performanceMetrics);

        return overview;
    }

    /**
     * Get user management data
     */
    public UserManagementResponse getUserManagement(String role, String status, int page, int size) {
        UserManagementResponse response = new UserManagementResponse();

        // Get users with filters
        List<User> users = getUsersWithFilters(role, status, page, size);
        response.setUsers(users.stream().map(this::convertToUserMap).collect(Collectors.toList()));

        // Statistics
        response.setTotalUsers(userRepository.count());
        response.setActiveUsers(getActiveUsersCount());
        response.setUsersByRole(getUsersByRoleStats());
        
        // Pagination
        response.setCurrentPage(page);
        response.setTotalSize(calculateTotalUsers(role, status));
        response.setTotalPages((int) Math.ceil((double) response.getTotalSize() / size));

        return response;
    }

    /**
     * Get audit logs
     */
    public AuditLogsResponse getAuditLogs(String action, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        AuditLogsResponse response = new AuditLogsResponse();

        // Get logs with filters
        List<AdminAuthService.AdminAuditLog> logs = getAuditLogsWithFilters(action, startDate, endDate, page, size);
        response.setLogs(logs);

        // Pagination
        response.setCurrentPage(page);
        response.setTotalLogs(calculateTotalLogs(action, startDate, endDate));
        response.setTotalPages((int) Math.ceil((double) response.getTotalLogs() / size));

        return response;
    }

    /**
     * Get configuration management
     */
    public ConfigurationManagementResponse getConfigurationManagement() {
        ConfigurationManagementResponse response = new ConfigurationManagementResponse();

        // System configurations
        Map<String, Object> systemConfigurations = getSystemConfigurations();
        response.setSystemConfigurations(systemConfigurations);

        // Service configurations
        Map<String, Object> serviceConfigurations = getServiceConfigurations();
        response.setServiceConfigurations(serviceConfigurations);

        // Security configurations
        Map<String, Object> securityConfigurations = getSecurityConfigurations();
        response.setSecurityConfigurations(securityConfigurations);

        // Recent changes
        List<Map<String, Object>> recentChanges = getRecentConfigurationChanges();
        response.setRecentChanges(recentChanges);

        return response;
    }

    /**
     * Update system configuration
     */
    public ConfigurationUpdateResponse updateConfiguration(String configKey, Map<String, Object> configValue, Long adminId) {
        try {
            // Validate configuration
            ValidationResult validation = validateConfigurationUpdate(configKey, configValue);
            if (!validation.isValid()) {
                return ConfigurationUpdateResponse.failure("Invalid configuration: " + String.join(", ", validation.getErrors()));
            }

            // Update configuration
            SystemConfiguration config = configRepository.findByKey(configKey)
                    .orElseGet(() -> {
                        SystemConfiguration newConfig = new SystemConfiguration();
                        newConfig.setConfigKey(configKey);
                        newConfig.setCreatedAt(LocalDateTime.now());
                        return newConfig;
                    });

            config.setConfigValue(configValue);
            config.setUpdatedBy(adminId);
            config.setUpdatedAt(LocalDateTime.now());
            configRepository.save(config);

            // Log configuration change
            logConfigurationChange(adminId, configKey, configValue);

            ConfigurationUpdateResponse response = new ConfigurationUpdateResponse();
            response.setSuccess(true);
            response.setMessage("Configuration updated successfully");
            response.setUpdatedConfiguration(configValue);

            return response;

        } catch (Exception e) {
            log.error("Failed to update configuration: {}", configKey, e);
            return ConfigurationUpdateResponse.failure("Failed to update configuration: " + e.getMessage());
        }
    }

    // Private helper methods
    private long getRunningServicesCount() {
        return serviceManagementService.getServices(new ServiceManagementService.ServiceFilter()).stream()
                .mapToLong(s -> s.getStatus() == ServiceManagementService.ServiceStatus.RUNNING ? 1 : 0)
                .sum();
    }

    private long getActiveUsersCount() {
        // Simplified - would check last login time
        return userRepository.count() / 2;
    }

    private String getSystemUptime() {
        // Simplified uptime calculation
        return "15 days, 7 hours, 32 minutes";
    }

    private double getCpuUsage() {
        // Simplified CPU usage
        return 45.2;
    }

    private double getMemoryUsage() {
        // Simplified memory usage
        return 67.8;
    }

    private double getDiskUsage() {
        // Simplified disk usage
        return 23.4;
    }

    private List<Map<String, Object>> getServiceStatusList() {
        List<Map<String, Object>> serviceStatus = new ArrayList<>();
        
        List<ServiceRegistry> services = serviceManagementService.getServices(new ServiceManagementService.ServiceFilter());
        
        for (ServiceRegistry service : services) {
            Map<String, Object> status = new HashMap<>();
            status.put("serviceName", service.getServiceName());
            status.put("displayName", service.getDisplayName());
            status.put("status", service.getStatus());
            status.put("version", service.getVersion());
            status.put("instances", service.getDesiredInstances());
            status.put("health", getServiceHealth(service.getId()));
            status.put("lastUpdated", service.getUpdatedAt());
            
            serviceStatus.add(status);
        }
        
        return serviceStatus;
    }

    private String getServiceHealth(Long serviceId) {
        ServiceManagementService.ServiceHealthStatus health = serviceManagementService.getServiceHealthStatus(serviceId);
        return health.getOverallStatus().toString();
    }

    private List<Map<String, Object>> getRecentAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Sample alerts
        Map<String, Object> alert1 = new HashMap<>();
        alert1.put("type", "HIGH_CPU");
        alert1.put("message", "Service 'user-service' CPU usage above 80%");
        alert1.put("severity", "WARNING");
        alert1.put("timestamp", LocalDateTime.now().minusMinutes(15));
        alerts.add(alert1);
        
        Map<String, Object> alert2 = new HashMap<>();
        alert2.put("type", "MEMORY_PRESSURE");
        alert2.put("message", "System memory usage approaching 90%");
        alert2.put("severity", "CRITICAL");
        alert2.put("timestamp", LocalDateTime.now().minusMinutes(45));
        alerts.add(alert2);
        
        return alerts;
    }

    private Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Response times
        Map<String, Object> responseTimes = new HashMap<>();
        responseTimes.put("average", 145.2);
        responseTimes.put("p95", 289.7);
        responseTimes.put("p99", 456.3);
        metrics.put("responseTimes", responseTimes);
        
        // Throughput
        Map<String, Object> throughput = new HashMap<>();
        throughput.put("requestsPerSecond", 1250.5);
        throughput.put("requestsPerMinute", 75030.0);
        throughput.put("requestsPerHour", 4501800.0);
        metrics.put("throughput", throughput);
        
        // Error rates
        Map<String, Object> errorRates = new HashMap<>();
        errorRates.put("totalErrorRate", 0.8);
        errorRates.put("clientErrorRate", 0.3);
        errorRates.put("serverErrorRate", 0.5);
        metrics.put("errorRates", errorRates);
        
        return metrics;
    }

    private List<User> getUsersWithFilters(String role, String status, int page, int size) {
        // Simplified user filtering
        return userRepository.findAll().stream()
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertToUserMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        userMap.put("status", user.getStatus());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("lastLoginAt", user.getLastLoginAt());
        userMap.put("active", user.isActive());
        return userMap;
    }

    private Map<String, Long> getUsersByRoleStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("USER", userRepository.countByRole("USER"));
        stats.put("CREATOR", userRepository.countByRole("CREATOR"));
        stats.put("ADMIN", userRepository.countByRole("ADMIN"));
        stats.put("SUPER_ADMIN", userRepository.countByRole("SUPER_ADMIN"));
        return stats;
    }

    private long calculateTotalUsers(String role, String status) {
        // Simplified calculation
        return userRepository.count();
    }

    private List<AdminAuthService.AdminAuditLog> getAuditLogsWithFilters(String action, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        // Simplified audit log filtering
        return auditLogRepository.findRecentLogs(100).stream()
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    private long calculateTotalLogs(String action, LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified calculation
        return auditLogRepository.count();
    }

    private Map<String, Object> getSystemConfigurations() {
        Map<String, Object> configs = new HashMap<>();
        
        configs.put("system.name", "Nexora Platform");
        configs.put("system.version", "2.0.0");
        configs.put("system.environment", "production");
        configs.put("system.timezone", "UTC");
        configs.put("system.maxUsers", 1000000);
        configs.put("system.sessionTimeout", 3600);
        
        return configs;
    }

    private Map<String, Object> getServiceConfigurations() {
        Map<String, Object> configs = new HashMap<>();
        
        configs.put("service.defaultInstances", 1);
        configs.put("service.maxInstances", 10);
        configs.put("service.healthCheckInterval", 30);
        configs.put("service.deploymentTimeout", 300);
        configs.put("service.autoScalingEnabled", false);
        
        return configs;
    }

    private Map<String, Object> getSecurityConfigurations() {
        Map<String, Object> configs = new HashMap<>();
        
        configs.put("security.passwordMinLength", 8);
        configs.put("security.sessionTimeout", 3600);
        configs.put("security.maxLoginAttempts", 5);
        configs.put("security.twoFactorEnabled", true);
        configs.put("security.encryptionEnabled", true);
        
        return configs;
    }

    private List<Map<String, Object>> getRecentConfigurationChanges() {
        List<Map<String, Object>> changes = new ArrayList<>();
        
        // Sample configuration changes
        Map<String, Object> change1 = new HashMap<>();
        change1.put("configKey", "service.defaultInstances");
        change1.put("oldValue", 1);
        change1.put("newValue", 2);
        change1.put("changedBy", 1L);
        change1.put("changedAt", LocalDateTime.now().minusHours(2));
        changes.add(change1);
        
        Map<String, Object> change2 = new HashMap<>();
        change2.put("configKey", "security.sessionTimeout");
        change2.put("oldValue", 1800);
        change2.put("newValue", 3600);
        change2.put("changedBy", 1L);
        change2.put("changedAt", LocalDateTime.now().minusDays(1));
        changes.add(change2);
        
        return changes;
    }

    private ValidationResult validateConfigurationUpdate(String configKey, Map<String, Object> configValue) {
        ValidationResult result = new ValidationResult();
        
        // Add validation logic based on config key
        if (configKey.startsWith("security.") && configValue == null) {
            result.addError("Security configurations cannot be null");
        }
        
        if (configKey.contains("timeout") && configValue instanceof Integer) {
            int timeout = (Integer) configValue;
            if (timeout < 0 || timeout > 86400) {
                result.addError("Timeout must be between 0 and 86400 seconds");
            }
        }
        
        return result;
    }

    private void logConfigurationChange(Long adminId, String configKey, Map<String, Object> configValue) {
        AdminAuthService.AdminAuditLog auditLog = new AdminAuthService.AdminAuditLog();
        auditLog.setAdminUserId(adminId);
        auditLog.setAction("CONFIG_UPDATED");
        auditLog.setDetails("Updated configuration: " + configKey);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    // Data classes
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
        private long totalSize;
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

        public static ConfigurationUpdateResponse failure(String message) {
            ConfigurationUpdateResponse response = new ConfigurationUpdateResponse();
            response.setSuccess(false);
            response.setMessage(message);
            return response;
        }
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

    // Entity classes
    @Data
    public static class User {
        private Long id;
        private String username;
        private String email;
        private String role;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
        private boolean active;
    }

    @Data
    public static class SystemConfiguration {
        private Long id;
        private String configKey;
        private Map<String, Object> configValue;
        private String description;
        private Long createdBy;
        private Long updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // Repository placeholders
    private static class UserRepository {
        public long count() { return 10000; }
        public long countByRole(String role) { return 2500; }
        public List<User> findAll() { 
            List<User> users = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                User user = new User();
                user.setId((long) i);
                user.setUsername("user" + i);
                user.setEmail("user" + i + "@example.com");
                user.setRole("USER");
                user.setStatus("ACTIVE");
                user.setCreatedAt(LocalDateTime.now().minusDays(i));
                user.setLastLoginAt(LocalDateTime.now().minusHours(i));
                user.setActive(true);
                users.add(user);
            }
            return users;
        }
    }

    private static class SystemConfigurationRepository {
        public Optional<SystemConfiguration> findByKey(String key) { return Optional.empty(); }
        public SystemConfiguration save(SystemConfiguration config) { return config; }
    }

    private static class AuditLogRepository {
        public List<AdminAuthService.AdminAuditLog> findRecentLogs(int limit) { return new ArrayList<>(); }
        public long count() { return 50000; }
        public AdminAuthService.AdminAuditLog save(AdminAuthService.AdminAuditLog log) { return log; }
    }

    // Service instances - duplicates removed
}

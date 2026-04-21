package org.example.nexora.admin.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Management System providing:
 * - Dynamic service registration and configuration
 * - Service health monitoring and status tracking
 * - Service dependency management
 * - Service scaling and load balancing
 * - Service versioning and deployment control
 * - Service metrics and analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceManagementService {

    private final ServiceRegistryRepository serviceRegistryRepository;
    private final ServiceInstanceRepository instanceRepository;
    private final ServiceConfigurationRepository configRepository;
    private final ServiceMetricsRepository metricsRepository;
    private final ServiceDeploymentRepository deploymentRepository;
    private final AdminAuditLogRepository auditLogRepository;

    // Service registry cache
    private final Map<String, ServiceRegistry> serviceCache = new HashMap<>();

    /**
     * Register new service
     */
    @Transactional
    public ServiceRegistrationResult registerService(ServiceRegistrationRequest request, Long adminId) {
        log.info("Registering new service: {} by admin: {}", request.getServiceName(), adminId);

        try {
            // Validate request
            ValidationResult validation = validateServiceRegistration(request);
            if (!validation.isValid()) {
                return ServiceRegistrationResult.failure(validation.getErrors());
            }

            // Check if service already exists
            if (serviceRegistryRepository.existsByServiceName(request.getServiceName())) {
                return ServiceRegistrationResult.failure("Service already exists");
            }

            // Create service registry entry
            ServiceRegistry service = new ServiceRegistry();
            service.setServiceName(request.getServiceName());
            service.setDisplayName(request.getDisplayName());
            service.setDescription(request.getDescription());
            service.setServiceType(request.getServiceType());
            service.setCategory(request.getCategory());
            service.setVersion(request.getVersion());
            service.setServiceClass(request.getServiceClass());
            service.setEndpoint(request.getEndpoint());
            service.setPort(request.getPort());
            service.setProtocol(request.getProtocol());
            service.setStatus(ServiceStatus.REGISTERED);
            service.setHealthCheckUrl(request.getHealthCheckUrl());
            service.setMetricsUrl(request.getMetricsUrl());
            service.setDependencies(request.getDependencies());
            service.setConfiguration(request.getConfiguration());
            service.setCreatedBy(adminId);
            service.setCreatedAt(LocalDateTime.now());

            // Set default configurations
            service.setAutoScalingEnabled(request.isAutoScalingEnabled());
            service.setMinInstances(request.getMinInstances() != null ? request.getMinInstances() : 1);
            service.setMaxInstances(request.getMaxInstances() != null ? request.getMaxInstances() : 10);
            service.setDesiredInstances(request.getDesiredInstances() != null ? request.getDesiredInstances() : 1);
            service.setHealthCheckInterval(request.getHealthCheckInterval() != null ? request.getHealthCheckInterval() : 30);
            service.setHealthCheckTimeout(request.getHealthCheckTimeout() != null ? request.getHealthCheckTimeout() : 5);

            service = serviceRegistryRepository.save(service);

            // Create default configuration
            createDefaultConfiguration(service, request);

            // Log registration
            logServiceAction(adminId, "SERVICE_REGISTERED", "Registered service: " + request.getServiceName(), service.getId());

            // Update cache
            serviceCache.put(service.getServiceName(), service);

            return ServiceRegistrationResult.success(service);

        } catch (Exception e) {
            log.error("Failed to register service: {}", request.getServiceName(), e);
            return ServiceRegistrationResult.failure("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Update service configuration
     */
    @Transactional
    public ServiceUpdateResult updateService(Long serviceId, ServiceUpdateRequest request, Long adminId) {
        log.info("Updating service: {} by admin: {}", serviceId, adminId);

        try {
            ServiceRegistry service = serviceRegistryRepository.findById(serviceId)
                    .orElseThrow(() -> new ServiceManagementException("Service not found"));

            // Validate request
            ValidationResult validation = validateServiceUpdate(request);
            if (!validation.isValid()) {
                return ServiceUpdateResult.failure(validation.getErrors());
            }

            // Update service fields
            if (request.getDisplayName() != null) {
                service.setDisplayName(request.getDisplayName());
            }
            if (request.getDescription() != null) {
                service.setDescription(request.getDescription());
            }
            if (request.getVersion() != null) {
                service.setVersion(request.getVersion());
            }
            if (request.getEndpoint() != null) {
                service.setEndpoint(request.getEndpoint());
            }
            if (request.getPort() != null) {
                service.setPort(request.getPort());
            }
            if (request.getHealthCheckUrl() != null) {
                service.setHealthCheckUrl(request.getHealthCheckUrl());
            }
            if (request.getConfiguration() != null) {
                service.setConfiguration(request.getConfiguration());
            }
            if (request.getMinInstances() != null) {
                service.setMinInstances(request.getMinInstances());
            }
            if (request.getMaxInstances() != null) {
                service.setMaxInstances(request.getMaxInstances());
            }
            if (request.getDesiredInstances() != null) {
                service.setDesiredInstances(request.getDesiredInstances());
            }
            if (request.isAutoScalingEnabled() != null) {
                service.setAutoScalingEnabled(request.isAutoScalingEnabled());
            }

            service.setUpdatedBy(adminId);
            service.setUpdatedAt(LocalDateTime.now());

            service = serviceRegistryRepository.save(service);

            // Update cache
            serviceCache.put(service.getServiceName(), service);

            // Log update
            logServiceAction(adminId, "SERVICE_UPDATED", "Updated service: " + service.getServiceName(), serviceId);

            return ServiceUpdateResult.success(service);

        } catch (Exception e) {
            log.error("Failed to update service: {}", serviceId, e);
            return ServiceUpdateResult.failure("Update failed: " + e.getMessage());
        }
    }

    /**
     * Remove service
     */
    @Transactional
    public ServiceRemovalResult removeService(Long serviceId, Long adminId) {
        log.info("Removing service: {} by admin: {}", serviceId, adminId);

        try {
            ServiceRegistry service = serviceRegistryRepository.findById(serviceId)
                    .orElseThrow(() -> new ServiceManagementException("Service not found"));

            // Check if service has active instances
            List<ServiceInstance> instances = instanceRepository.findByServiceIdAndStatus(serviceId, InstanceStatus.RUNNING);
            if (!instances.isEmpty()) {
                return ServiceRemovalResult.failure("Cannot remove service with " + instances.size() + " running instances");
            }

            // Check service dependencies
            List<ServiceRegistry> dependentServices = findDependentServices(serviceId);
            if (!dependentServices.isEmpty()) {
                return ServiceRemovalResult.failure("Service has " + dependentServices.size() + " dependent services");
            }

            // Remove service
            service.setStatus(ServiceStatus.DEPRECATED);
            service.setUpdatedBy(adminId);
            service.setUpdatedAt(LocalDateTime.now());
            serviceRegistryRepository.save(service);

            // Remove configurations
            configRepository.deleteByServiceId(serviceId);

            // Remove metrics
            metricsRepository.deleteByServiceId(serviceId);

            // Remove from cache
            serviceCache.remove(service.getServiceName());

            // Log removal
            logServiceAction(adminId, "SERVICE_REMOVED", "Removed service: " + service.getServiceName(), serviceId);

            return ServiceRemovalResult.success(service);

        } catch (Exception e) {
            log.error("Failed to remove service: {}", serviceId, e);
            return ServiceRemovalResult.failure("Removal failed: " + e.getMessage());
        }
    }

    /**
     * Deploy service
     */
    @Transactional
    public ServiceDeploymentResult deployService(Long serviceId, ServiceDeploymentRequest request, Long adminId) {
        log.info("Deploying service: {} by admin: {}", serviceId, adminId);

        try {
            ServiceRegistry service = serviceRegistryRepository.findById(serviceId)
                    .orElseThrow(() -> new ServiceManagementException("Service not found"));

            // Validate deployment request
            ValidationResult validation = validateDeploymentRequest(service, request);
            if (!validation.isValid()) {
                return ServiceDeploymentResult.failure(validation.getErrors());
            }

            // Create deployment record
            ServiceDeployment deployment = new ServiceDeployment();
            deployment.setServiceId(serviceId);
            deployment.setVersion(request.getVersion());
            deployment.setDeploymentType(request.getDeploymentType());
            deployment.setEnvironment(request.getEnvironment());
            deployment.setDeploymentConfig(request.getDeploymentConfig());
            deployment.setStatus(DeploymentStatus.PENDING);
            deployment.setRequestedBy(adminId);
            deployment.setRequestedAt(LocalDateTime.now());

            deployment = deploymentRepository.save(deployment);

            // Start deployment process
            startDeploymentProcess(deployment, service, request);

            // Update service status
            service.setStatus(ServiceStatus.DEPLOYING);
            service.setUpdatedBy(adminId);
            service.setUpdatedAt(LocalDateTime.now());
            serviceRegistryRepository.save(service);

            // Log deployment
            logServiceAction(adminId, "SERVICE_DEPLOYED", "Deployed service: " + service.getServiceName() + " version: " + request.getVersion(), serviceId);

            return ServiceDeploymentResult.success(deployment);

        } catch (Exception e) {
            log.error("Failed to deploy service: {}", serviceId, e);
            return ServiceDeploymentResult.failure("Deployment failed: " + e.getMessage());
        }
    }

    /**
     * Scale service instances
     */
    @Transactional
    public ServiceScalingResult scaleService(Long serviceId, ServiceScalingRequest request, Long adminId) {
        log.info("Scaling service: {} to {} instances by admin: {}", serviceId, request.getTargetInstances, adminId);

        try {
            ServiceRegistry service = serviceRegistryRepository.findById(serviceId)
                    .orElseThrow(() -> new ServiceManagementException("Service not found"));

            // Validate scaling request
            if (request.getTargetInstances < service.getMinInstances() || request.getTargetInstances > service.getMaxInstances()) {
                return ServiceScalingResult.failure("Target instances must be between " + service.getMinInstances() + " and " + service.getMaxInstances());
            }

            int currentInstances = instanceRepository.countByServiceIdAndStatus(serviceId, InstanceStatus.RUNNING);
            int instancesToAdd = request.getTargetInstances - currentInstances;

            if (instancesToAdd == 0) {
                return ServiceScalingResult.success("Service already has " + currentInstances + " instances");
            }

            // Scale up or down
            if (instancesToAdd > 0) {
                scaleUpService(service, instancesToAdd);
            } else {
                scaleDownService(service, Math.abs(instancesToAdd));
            }

            // Update desired instances
            service.setDesiredInstances(request.getTargetInstances);
            service.setUpdatedBy(adminId);
            service.setUpdatedAt(LocalDateTime.now());
            serviceRegistryRepository.save(service);

            // Log scaling
            logServiceAction(adminId, "SERVICE_SCALED", 
                           "Scaled service: " + service.getServiceName() + " from " + currentInstances + " to " + request.getTargetInstances + " instances", 
                           serviceId);

            return ServiceScalingResult.success("Service scaled to " + request.getTargetInstances + " instances");

        } catch (Exception e) {
            log.error("Failed to scale service: {}", serviceId, e);
            return ServiceScalingResult.failure("Scaling failed: " + e.getMessage());
        }
    }

    /**
     * Get service registry
     */
    public List<ServiceRegistry> getServices(ServiceFilter filter) {
        List<ServiceRegistry> services = serviceRegistryRepository.findAll();

        // Apply filters
        if (filter.getCategory() != null) {
            services = services.stream()
                    .filter(s -> filter.getCategory().equals(s.getCategory()))
                    .collect(Collectors.toList());
        }

        if (filter.getStatus() != null) {
            services = services.stream()
                    .filter(s -> filter.getStatus().equals(s.getStatus()))
                    .collect(Collectors.toList());
        }

        if (filter.getServiceType() != null) {
            services = services.stream()
                    .filter(s -> filter.getServiceType().equals(s.getServiceType()))
                    .collect(Collectors.toList());
        }

        return services;
    }

    /**
     * Get service details
     */
    public ServiceDetail getServiceDetail(Long serviceId) {
        ServiceRegistry service = serviceRegistryRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceManagementException("Service not found"));

        ServiceDetail detail = new ServiceDetail();
        detail.setService(service);

        // Get instances
        List<ServiceInstance> instances = instanceRepository.findByServiceId(serviceId);
        detail.setInstances(instances);

        // Get configurations
        List<ServiceConfiguration> configurations = configRepository.findByServiceId(serviceId);
        detail.setConfigurations(configurations);

        // Get recent metrics
        List<ServiceMetrics> recentMetrics = metricsRepository.findRecentByServiceId(serviceId, 24);
        detail.setRecentMetrics(recentMetrics);

        // Get deployments
        List<ServiceDeployment> deployments = deploymentRepository.findByServiceIdOrderByRequestedAtDesc(serviceId);
        detail.setDeployments(deployments);

        // Get dependencies
        List<ServiceRegistry> dependencies = getServiceDependencies(serviceId);
        detail.setDependencies(dependencies);

        // Get dependent services
        List<ServiceRegistry> dependentServices = findDependentServices(serviceId);
        detail.setDependentServices(dependentServices);

        return detail;
    }

    /**
     * Get service health status
     */
    public ServiceHealthStatus getServiceHealthStatus(Long serviceId) {
        ServiceRegistry service = serviceRegistryRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceManagementException("Service not found"));

        ServiceHealthStatus healthStatus = new ServiceHealthStatus();
        healthStatus.setServiceId(serviceId);
        healthStatus.setServiceName(service.getServiceName());
        healthStatus.setOverallStatus(calculateOverallHealth(serviceId));
        healthStatus.setLastHealthCheck(LocalDateTime.now());

        // Get instance health
        List<ServiceInstance> instances = instanceRepository.findByServiceId(serviceId);
        healthStatus.setTotalInstances(instances.size());
        healthStatus.setHealthyInstances(instances.stream().mapToInt(i -> i.isHealthy() ? 1 : 0).sum());
        healthStatus.setUnhealthyInstances(instances.stream().mapToInt(i -> !i.isHealthy() ? 1 : 0).sum());

        // Calculate health percentage
        if (healthStatus.getTotalInstances() > 0) {
            healthStatus.setHealthPercentage((double) healthStatus.getHealthyInstances() / healthStatus.getTotalInstances() * 100);
        }

        // Get recent health checks
        List<HealthCheck> recentHealthChecks = getRecentHealthChecks(serviceId, 60);
        healthStatus.setRecentHealthChecks(recentHealthChecks);

        return healthStatus;
    }

    /**
     * Get service metrics
     */
    public ServiceMetricsSummary getServiceMetrics(Long serviceId, MetricsRequest request) {
        ServiceMetricsSummary summary = new ServiceMetricsSummary();
        summary.setServiceId(serviceId);
        summary.setTimeRange(request.getTimeRange());
        summary.setGeneratedAt(LocalDateTime.now());

        // Get metrics for time range
        List<ServiceMetrics> metrics = metricsRepository.findByServiceIdAndTimeRange(
                serviceId, request.getStartTime(), request.getEndTime());

        if (metrics.isEmpty()) {
            return summary;
        }

        // Calculate aggregates
        summary.setTotalRequests(metrics.stream().mapToLong(ServiceMetrics::getRequestCount).sum());
        summary.setAverageResponseTime(metrics.stream().mapToDouble(ServiceMetrics::getAverageResponseTime).average().orElse(0.0));
        summary.setSuccessRate(calculateSuccessRate(metrics));
        summary.setErrorRate(100.0 - summary.getSuccessRate());
        summary.setThroughput(calculateThroughput(metrics, request.getTimeRange()));

        // Get time series data
        summary.setTimeSeriesData(createTimeSeriesData(metrics));

        return summary;
    }

    // Private helper methods
    private ValidationResult validateServiceRegistration(ServiceRegistrationRequest request) {
        ValidationResult result = new ValidationResult();

        if (request.getServiceName() == null || request.getServiceName().trim().isEmpty()) {
            result.addError("Service name is required");
        }

        if (request.getServiceClass() == null || request.getServiceClass().trim().isEmpty()) {
            result.addError("Service class is required");
        }

        if (request.getEndpoint() == null || request.getEndpoint().trim().isEmpty()) {
            result.addError("Service endpoint is required");
        }

        if (request.getPort() == null || request.getPort() < 1 || request.getPort() > 65535) {
            result.addError("Valid port is required");
        }

        if (request.getVersion() == null || request.getVersion().trim().isEmpty()) {
            result.addError("Service version is required");
        }

        return result;
    }

    private ValidationResult validateServiceUpdate(ServiceUpdateRequest request) {
        ValidationResult result = new ValidationResult();
        // Add validation logic as needed
        return result;
    }

    private ValidationResult validateDeploymentRequest(ServiceRegistry service, ServiceDeploymentRequest request) {
        ValidationResult result = new ValidationResult();

        if (request.getVersion() == null || request.getVersion().trim().isEmpty()) {
            result.addError("Deployment version is required");
        }

        if (request.getDeploymentType() == null) {
            result.addError("Deployment type is required");
        }

        if (request.getEnvironment() == null) {
            result.addError("Deployment environment is required");
        }

        return result;
    }

    private void createDefaultConfiguration(ServiceRegistry service, ServiceRegistrationRequest request) {
        ServiceConfiguration config = new ServiceConfiguration();
        config.setServiceId(service.getId());
        config.setConfigKey("default");
        config.setConfigValue(request.getConfiguration() != null ? request.getConfiguration() : new HashMap<>());
        config.setEnvironment("production");
        config.setVersion("1.0");
        config.setCreatedAt(LocalDateTime.now());
        configRepository.save(config);
    }

    private void startDeploymentProcess(ServiceDeployment deployment, ServiceRegistry service, ServiceDeploymentRequest request) {
        // Simplified deployment process
        deployment.setStatus(DeploymentStatus.IN_PROGRESS);
        deployment.setStartedAt(LocalDateTime.now());
        deploymentRepository.save(deployment);

        // In a real implementation, this would trigger actual deployment
        // For now, we'll simulate deployment completion
        completeDeployment(deployment);
    }

    private void completeDeployment(ServiceDeployment deployment) {
        deployment.setStatus(DeploymentStatus.COMPLETED);
        deployment.setCompletedAt(LocalDateTime.now());
        deploymentRepository.save(deployment);

        // Update service status
        ServiceRegistry service = serviceRegistryRepository.findById(deployment.getServiceId()).orElse(null);
        if (service != null) {
            service.setStatus(ServiceStatus.RUNNING);
            service.setUpdatedAt(LocalDateTime.now());
            serviceRegistryRepository.save(service);
        }
    }

    private void scaleUpService(ServiceRegistry service, int instancesToAdd) {
        for (int i = 0; i < instancesToAdd; i++) {
            ServiceInstance instance = new ServiceInstance();
            instance.setServiceId(service.getId());
            instance.setInstanceId(UUID.randomUUID().toString());
            instance.setHost("server-" + (i + 1));
            instance.setPort(service.getPort());
            instance.setStatus(InstanceStatus.STARTING);
            instance.setCreatedAt(LocalDateTime.now());
            instanceRepository.save(instance);

            // Simulate instance startup
            instance.setStatus(InstanceStatus.RUNNING);
            instance.setStartedAt(LocalDateTime.now());
            instance.setHealthy(true);
            instanceRepository.save(instance);
        }
    }

    private void scaleDownService(ServiceRegistry service, int instancesToRemove) {
        List<ServiceInstance> instances = instanceRepository.findByServiceIdAndStatus(service.getId(), InstanceStatus.RUNNING);
        
        for (int i = 0; i < Math.min(instancesToRemove, instances.size()); i++) {
            ServiceInstance instance = instances.get(i);
            instance.setStatus(InstanceStatus.TERMINATING);
            instance.setTerminatedAt(LocalDateTime.now());
            instanceRepository.save(instance);
        }
    }

    private List<ServiceRegistry> findDependentServices(Long serviceId) {
        // Find services that depend on this service
        return serviceRegistryRepository.findAll().stream()
                .filter(s -> s.getDependencies() != null && s.getDependencies().contains(serviceId.toString()))
                .collect(Collectors.toList());
    }

    private List<ServiceRegistry> getServiceDependencies(Long serviceId) {
        ServiceRegistry service = serviceRegistryRepository.findById(serviceId).orElse(null);
        if (service == null || service.getDependencies() == null) {
            return new ArrayList<>();
        }

        return service.getDependencies().stream()
                .map(depId -> {
                    try {
                        Long id = Long.parseLong(depId);
                        return serviceRegistryRepository.findById(id).orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ServiceStatus calculateOverallHealth(Long serviceId) {
        List<ServiceInstance> instances = instanceRepository.findByServiceId(serviceId);
        
        if (instances.isEmpty()) {
            return ServiceStatus.STOPPED;
        }

        long healthyInstances = instances.stream().mapToInt(i -> i.isHealthy() ? 1 : 0).sum();
        
        if (healthyInstances == instances.size()) {
            return ServiceStatus.RUNNING;
        } else if (healthyInstances > 0) {
            return ServiceStatus.DEGRADED;
        } else {
            return ServiceStatus.FAILED;
        }
    }

    private List<HealthCheck> getRecentHealthChecks(Long serviceId, int minutes) {
        // Simplified - would fetch from health check logs
        List<HealthCheck> checks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            HealthCheck check = new HealthCheck();
            check.setTimestamp(LocalDateTime.now().minusMinutes(i));
            check.setHealthy(Math.random() > 0.1); // 90% healthy
            check.setResponseTime(Math.random() * 100);
            checks.add(check);
        }
        return checks;
    }

    private double calculateSuccessRate(List<ServiceMetrics> metrics) {
        long totalRequests = metrics.stream().mapToLong(ServiceMetrics::getRequestCount).sum();
        long totalErrors = metrics.stream().mapToLong(ServiceMetrics::getErrorCount).sum();
        
        if (totalRequests == 0) return 100.0;
        return (double) (totalRequests - totalErrors) / totalRequests * 100;
    }

    private double calculateThroughput(List<ServiceMetrics> metrics, String timeRange) {
        long totalRequests = metrics.stream().mapToLong(ServiceMetrics::getRequestCount).sum();
        
        // Calculate time range in hours
        double hours = 24.0; // Default to 24 hours
        if ("1h".equals(timeRange)) hours = 1.0;
        else if ("6h".equals(timeRange)) hours = 6.0;
        else if ("24h".equals(timeRange)) hours = 24.0;
        else if ("7d".equals(timeRange)) hours = 168.0;
        
        return totalRequests / hours;
    }

    private List<TimeSeriesDataPoint> createTimeSeriesData(List<ServiceMetrics> metrics) {
        return metrics.stream()
                .map(m -> {
                    TimeSeriesDataPoint point = new TimeSeriesDataPoint();
                    point.setTimestamp(m.getTimestamp());
                    point.setRequestCount(m.getRequestCount());
                    point.setAverageResponseTime(m.getAverageResponseTime());
                    point.setErrorRate(m.getErrorCount() * 100.0 / m.getRequestCount());
                    return point;
                })
                .collect(Collectors.toList());
    }

    private void logServiceAction(Long adminId, String action, String details, Long serviceId) {
        AdminAuditLog auditLog = new AdminAuditLog();
        auditLog.setAdminUserId(adminId);
        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setServiceId(serviceId);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    // Data classes
    @Data
    public static class ServiceRegistrationResult {
        private boolean success;
        private ServiceRegistry service;
        private List<String> errors;

        public static ServiceRegistrationResult success(ServiceRegistry service) {
            ServiceRegistrationResult result = new ServiceRegistrationResult();
            result.setSuccess(true);
            result.setService(service);
            return result;
        }

        public static ServiceRegistrationResult failure(String error) {
            ServiceRegistrationResult result = new ServiceRegistrationResult();
            result.setSuccess(false);
            result.setErrors(Arrays.asList(error));
            return result;
        }

        public static ServiceRegistrationResult failure(List<String> errors) {
            ServiceRegistrationResult result = new ServiceRegistrationResult();
            result.setSuccess(false);
            result.setErrors(errors);
            return result;
        }
    }

    @Data
    public static class ServiceUpdateResult {
        private boolean success;
        private ServiceRegistry service;
        private List<String> errors;

        public static ServiceUpdateResult success(ServiceRegistry service) {
            ServiceUpdateResult result = new ServiceUpdateResult();
            result.setSuccess(true);
            result.setService(service);
            return result;
        }

        public static ServiceUpdateResult failure(String error) {
            ServiceUpdateResult result = new ServiceUpdateResult();
            result.setSuccess(false);
            result.setErrors(Arrays.asList(error));
            return result;
        }

        public static ServiceUpdateResult failure(List<String> errors) {
            ServiceUpdateResult result = new ServiceUpdateResult();
            result.setSuccess(false);
            result.setErrors(errors);
            return result;
        }
    }

    @Data
    public static class ServiceRemovalResult {
        private boolean success;
        private ServiceRegistry service;
        private List<String> errors;

        public static ServiceRemovalResult success(ServiceRegistry service) {
            ServiceRemovalResult result = new ServiceRemovalResult();
            result.setSuccess(true);
            result.setService(service);
            return result;
        }

        public static ServiceRemovalResult failure(String error) {
            ServiceRemovalResult result = new ServiceRemovalResult();
            result.setSuccess(false);
            result.setErrors(Arrays.asList(error));
            return result;
        }
    }

    @Data
    public static class ServiceDeploymentResult {
        private boolean success;
        private ServiceDeployment deployment;
        private List<String> errors;

        public static ServiceDeploymentResult success(ServiceDeployment deployment) {
            ServiceDeploymentResult result = new ServiceDeploymentResult();
            result.setSuccess(true);
            result.setDeployment(deployment);
            return result;
        }

        public static ServiceDeploymentResult failure(String error) {
            ServiceDeploymentResult result = new ServiceDeploymentResult();
            result.setSuccess(false);
            result.setErrors(Arrays.asList(error));
            return result;
        }
    }

    @Data
    public static class ServiceScalingResult {
        private boolean success;
        private String message;
        private List<String> errors;

        public static ServiceScalingResult success(String message) {
            ServiceScalingResult result = new ServiceScalingResult();
            result.setSuccess(true);
            result.setMessage(message);
            return result;
        }

        public static ServiceScalingResult failure(String error) {
            ServiceScalingResult result = new ServiceScalingResult();
            result.setSuccess(false);
            result.setErrors(Arrays.asList(error));
            return result;
        }
    }

    @Data
    public static class ServiceDetail {
        private ServiceRegistry service;
        private List<ServiceInstance> instances;
        private List<ServiceConfiguration> configurations;
        private List<ServiceMetrics> recentMetrics;
        private List<ServiceDeployment> deployments;
        private List<ServiceRegistry> dependencies;
        private List<ServiceRegistry> dependentServices;
    }

    @Data
    public static class ServiceHealthStatus {
        private Long serviceId;
        private String serviceName;
        private ServiceStatus overallStatus;
        private int totalInstances;
        private int healthyInstances;
        private int unhealthyInstances;
        private double healthPercentage;
        private LocalDateTime lastHealthCheck;
        private List<HealthCheck> recentHealthChecks;
    }

    @Data
    public static class ServiceMetricsSummary {
        private Long serviceId;
        private String timeRange;
        private LocalDateTime generatedAt;
        private long totalRequests;
        private double averageResponseTime;
        private double successRate;
        private double errorRate;
        private double throughput;
        private List<TimeSeriesDataPoint> timeSeriesData;
    }

    @Data
    public static class TimeSeriesDataPoint {
        private LocalDateTime timestamp;
        private long requestCount;
        private double averageResponseTime;
        private double errorRate;
    }

    @Data
    public static class HealthCheck {
        private LocalDateTime timestamp;
        private boolean healthy;
        private double responseTime;
        private String message;
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
    public static class ServiceRegistrationRequest {
        private String serviceName;
        private String displayName;
        private String description;
        private ServiceType serviceType;
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
        private Integer healthCheckInterval = 30;
        private Integer healthCheckTimeout = 5;
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
        private DeploymentType deploymentType;
        private String environment;
        private Map<String, Object> deploymentConfig;
        private boolean rollbackOnFailure = true;
    }

    @Data
    public static class ServiceScalingRequest {
        private int targetInstances;
        private ScalingReason reason;
        private String comment;
    }

    @Data
    public static class ServiceFilter {
        private String category;
        private ServiceStatus status;
        private ServiceType serviceType;
        private String environment;
    }

    @Data
    public static class MetricsRequest {
        private String timeRange;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<String> metrics;
    }

    // Entity classes
    @Data
    public static class ServiceRegistry {
        private Long id;
        private String serviceName;
        private String displayName;
        private String description;
        private ServiceType serviceType;
        private String category;
        private String version;
        private String serviceClass;
        private String endpoint;
        private Integer port;
        private String protocol;
        private ServiceStatus status;
        private String healthCheckUrl;
        private String metricsUrl;
        private List<String> dependencies;
        private Map<String, Object> configuration;
        private Boolean autoScalingEnabled;
        private Integer minInstances;
        private Integer maxInstances;
        private Integer desiredInstances;
        private Integer healthCheckInterval;
        private Integer healthCheckTimeout;
        private Long createdBy;
        private Long updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class ServiceInstance {
        private Long id;
        private Long serviceId;
        private String instanceId;
        private String host;
        private Integer port;
        private InstanceStatus status;
        private boolean healthy;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime terminatedAt;
        private Map<String, Object> metadata;
    }

    @Data
    public static class ServiceConfiguration {
        private Long id;
        private Long serviceId;
        private String configKey;
        private Map<String, Object> configValue;
        private String environment;
        private String version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class ServiceMetrics {
        private Long id;
        private Long serviceId;
        private Long instanceId;
        private LocalDateTime timestamp;
        private long requestCount;
        private long errorCount;
        private double averageResponseTime;
        private double p95ResponseTime;
        private double p99ResponseTime;
        private double cpuUsage;
        private double memoryUsage;
        private double networkIn;
        private double networkOut;
    }

    @Data
    public static class ServiceDeployment {
        private Long id;
        private Long serviceId;
        private String version;
        private DeploymentType deploymentType;
        private String environment;
        private DeploymentStatus status;
        private Map<String, Object> deploymentConfig;
        private Long requestedBy;
        private LocalDateTime requestedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
    }

    @Data
    public static class AdminAuditLog {
        private Long id;
        private Long adminUserId;
        private String action;
        private String details;
        private Long serviceId;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;
    }

    // Enums
    public enum ServiceType {
        MICROSERVICE, DATABASE, CACHE, QUEUE, SEARCH, STORAGE, MONITORING, AUTHENTICATION
    }

    public enum ServiceStatus {
        REGISTERED, DEPLOYING, RUNNING, STOPPED, FAILED, DEGRADED, DEPRECATED
    }

    public enum InstanceStatus {
        STARTING, RUNNING, STOPPING, TERMINATED, FAILED
    }

    public enum DeploymentType {
        ROLLING, BLUE_GREEN, CANARY, RECREATE
    }

    public enum DeploymentStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, ROLLED_BACK
    }

    public enum ScalingReason {
        MANUAL, AUTO_SCALE, HEALTH_CHECK, LOAD_BALANCING
    }

    // Repository placeholders
    private static class ServiceRegistryRepository {
        public boolean existsByServiceName(String serviceName) { return false; }
        public Optional<ServiceRegistry> findById(Long id) { return Optional.empty(); }
        public ServiceRegistry save(ServiceRegistry service) { return service; }
        public List<ServiceRegistry> findAll() { return new ArrayList<>(); }
        public List<ServiceRegistry> findByServiceName(String serviceName) { return new ArrayList<>(); }
    }

    private static class ServiceInstanceRepository {
        public List<ServiceInstance> findByServiceId(Long serviceId) { return new ArrayList<>(); }
        public List<ServiceInstance> findByServiceIdAndStatus(Long serviceId, InstanceStatus status) { return new ArrayList<>(); }
        public int countByServiceIdAndStatus(Long serviceId, InstanceStatus status) { return 0; }
        public ServiceInstance save(ServiceInstance instance) { return instance; }
    }

    private static class ServiceConfigurationRepository {
        public List<ServiceConfiguration> findByServiceId(Long serviceId) { return new ArrayList<>(); }
        public ServiceConfiguration save(ServiceConfiguration config) { return config; }
        public void deleteByServiceId(Long serviceId) {}
    }

    private static class ServiceMetricsRepository {
        public List<ServiceMetrics> findRecentByServiceId(Long serviceId, int hours) { return new ArrayList<>(); }
        public List<ServiceMetrics> findByServiceIdAndTimeRange(Long serviceId, LocalDateTime start, LocalDateTime end) { return new ArrayList<>(); }
        public void deleteByServiceId(Long serviceId) {}
    }

    private static class ServiceDeploymentRepository {
        public ServiceDeployment save(ServiceDeployment deployment) { return deployment; }
        public List<ServiceDeployment> findByServiceIdOrderByRequestedAtDesc(Long serviceId) { return new ArrayList<>(); }
    }

    private static class AdminAuditLogRepository {
        public AdminAuditLog save(AdminAuditLog log) { return log; }
    }

    // Service instances - duplicates removed
}

class ServiceManagementException extends RuntimeException {
    public ServiceManagementException(String message) {
        super(message);
    }
}

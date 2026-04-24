package org.example.nexora.admin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service registry entity
 */
public class ServiceRegistry {

    /**
     * Lifecycle status values for a registered service.
     */
    public enum ServiceStatus {
        ACTIVE, INACTIVE, DEPLOYING, DEPRECATED, FAILED,
        RUNNING, STOPPED, DEGRADED, MAINTENANCE, ERROR, STARTING, STOPPING
    }

    private Long id;
    private String serviceName;
    private String displayName;
    private String description;
    private String serviceType;
    private String category;
    private String version;
    private ServiceStatus status;
    private String host;
    private int port;
    private String serviceClass;
    private String endpoint;
    private String protocol;
    private String healthCheckUrl;
    private String metricsUrl;
    private String[] dependencies;
    private LocalDateTime registeredAt;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
    private boolean healthy;
    private Integer desiredInstances;
    private Integer minInstances;
    private Integer maxInstances;
    private Integer healthCheckInterval;
    private Integer healthCheckTimeout;

    public ServiceRegistry() {
        this.registeredAt = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
        this.status = ServiceStatus.ACTIVE;
        this.healthy = true;
        this.metadata = new HashMap<>();
    }

    public boolean isHealthy() {
        return healthy && ServiceStatus.ACTIVE.equals(status);
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }

    // --- Getters ---

    public Long getId() { return id; }
    public String getServiceName() { return serviceName; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getServiceType() { return serviceType; }
    public String getCategory() { return category; }
    public String getVersion() { return version; }
    public ServiceStatus getStatus() { return status; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getServiceClass() { return serviceClass; }
    public String getEndpoint() { return endpoint; }
    public String getProtocol() { return protocol; }
    public String getHealthCheckUrl() { return healthCheckUrl; }
    public String getMetricsUrl() { return metricsUrl; }
    public String[] getDependencies() { return dependencies; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Map<String, Object> getConfiguration() { return metadata; }
    public Integer getHealthCheckInterval() { return healthCheckInterval; }
    public Integer getHealthCheckTimeout() { return healthCheckTimeout; }

    public Integer getMinInstances() {
        if (minInstances != null) return minInstances;
        Object val = metadata != null ? metadata.get("minInstances") : null;
        return val instanceof Integer ? (Integer) val : null;
    }

    public Integer getMaxInstances() {
        if (maxInstances != null) return maxInstances;
        Object val = metadata != null ? metadata.get("maxInstances") : null;
        return val instanceof Integer ? (Integer) val : null;
    }

    public Integer getDesiredInstances() {
        if (desiredInstances != null) return desiredInstances;
        Object val = metadata != null ? metadata.get("desiredInstances") : null;
        return val instanceof Integer ? (Integer) val : null;
    }

    public Boolean isAutoScalingEnabled() {
        Object val = metadata != null ? metadata.get("autoScalingEnabled") : null;
        return val instanceof Boolean ? (Boolean) val : null;
    }

    // --- Setters ---

    public void setId(Long id) { this.id = id; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setCategory(String category) { this.category = category; }
    public void setVersion(String version) { this.version = version; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setServiceClass(String serviceClass) { this.serviceClass = serviceClass; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setHealthCheckUrl(String healthCheckUrl) { this.healthCheckUrl = healthCheckUrl; }
    public void setMetricsUrl(String metricsUrl) { this.metricsUrl = metricsUrl; }
    public void setDependencies(String[] dependencies) { this.dependencies = dependencies; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public void setMinInstances(Integer minInstances) { this.minInstances = minInstances; }
    public void setMaxInstances(Integer maxInstances) { this.maxInstances = maxInstances; }
    public void setDesiredInstances(Integer desiredInstances) { this.desiredInstances = desiredInstances; }
    public void setHealthCheckInterval(Integer healthCheckInterval) { this.healthCheckInterval = healthCheckInterval; }
    public void setHealthCheckTimeout(Integer healthCheckTimeout) { this.healthCheckTimeout = healthCheckTimeout; }

    public void setConfiguration(Map<String, Object> configuration) {
        this.metadata = configuration != null ? configuration : new HashMap<>();
    }

    public void setCreatedBy(Long createdBy) {
        ensureMetadata();
        this.metadata.put("createdBy", createdBy);
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.registeredAt = createdAt;
    }

    public void setUpdatedBy(Long updatedBy) {
        ensureMetadata();
        this.metadata.put("updatedBy", updatedBy);
    }

    public void setAutoScalingEnabled(boolean autoScalingEnabled) {
        ensureMetadata();
        this.metadata.put("autoScalingEnabled", autoScalingEnabled);
    }

    public void setLoadBalancerType(String loadBalancerType) {
        ensureMetadata();
        this.metadata.put("loadBalancerType", loadBalancerType);
    }

    private void ensureMetadata() {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
    }
}

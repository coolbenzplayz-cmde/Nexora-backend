package org.example.nexora.admin;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * Service registry entity
 */
@Data
public class ServiceRegistry {
    
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
    
    public ServiceRegistry() {
        this.registeredAt = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
        this.status = ServiceStatus.ACTIVE;
        this.healthy = true;
    }
    
    public boolean isHealthy() {
        return healthy && ServiceStatus.ACTIVE.equals(status);
    }
    
    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public void setStatus(ServiceStatus status) {
        this.status = status.toString();
    }
    
    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }
    
    public void setMetricsUrl(String metricsUrl) {
        this.metricsUrl = metricsUrl;
    }
    
    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }
    
    public void setConfiguration(Map<String, Object> configuration) {
        this.metadata = configuration;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.metadata.put("createdBy", createdBy);
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.registeredAt = createdAt;
    }
    
    public void setAutoScalingEnabled(boolean autoScalingEnabled) {
        this.metadata.put("autoScalingEnabled", autoScalingEnabled);
    }
    
    public void setMinInstances(Integer minInstances) {
        this.metadata.put("minInstances", minInstances);
    }
    
    public void setMaxInstances(Integer maxInstances) {
        this.metadata.put("maxInstances", maxInstances);
    }
    
    public void setLoadBalancerType(String loadBalancerType) {
        this.metadata.put("loadBalancerType", loadBalancerType);
    }
    
    public void setDesiredInstances(Integer desiredInstances) {
        this.metadata.put("desiredInstances", desiredInstances);
    }
    
    public void setHealthCheckInterval(Integer healthCheckInterval) {
        this.metadata.put("healthCheckInterval", healthCheckInterval);
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public ServiceStatus getStatus() {
        return status;
    }
    
    public Integer getDesiredInstances() {
        return desiredInstances != null ? desiredInstances : (Integer) metadata.get("desiredInstances");
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.metadata.put("updatedBy", updatedBy);
    }
    
    public void setStatus(ServiceStatus status) {
        this.status = status;
    }
    
    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }
    
    public Map<String, Object> getConfiguration() {
        return metadata;
    }
    
    public Integer getMinInstances() {
        return minInstances != null ? minInstances : (Integer) metadata.get("minInstances");
    }
    
    public Integer getMaxInstances() {
        return maxInstances != null ? maxInstances : (Integer) metadata.get("maxInstances");
    }
    
    public Boolean isAutoScalingEnabled() {
        return (Boolean) metadata.get("autoScalingEnabled");
    }
}

enum ServiceStatus {
    ACTIVE, INACTIVE, MAINTENANCE, ERROR, STARTING, STOPPING, DEPRECATED
}

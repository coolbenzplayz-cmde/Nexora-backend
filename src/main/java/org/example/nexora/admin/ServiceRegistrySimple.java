package org.example.nexora.admin;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simple service registry without Lombok to avoid compilation issues
 */
public class ServiceRegistrySimple {
    
    private String serviceName;
    private String displayName;
    private String description;
    private String serviceType;
    private String category;
    private String version;
    private String serviceClass;
    private String endpoint;
    private int port;
    private String protocol;
    private String status;
    private String healthCheckUrl;
    private String metricsUrl;
    private String[] dependencies;
    private Map<String, Object> metadata;
    private LocalDateTime registeredAt;
    private LocalDateTime lastHeartbeat;
    private boolean healthy;
    
    public ServiceRegistrySimple() {
        this.status = "ACTIVE";
        this.healthy = true;
        this.registeredAt = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
        this.metadata = new java.util.HashMap<>();
    }
    
    // Getters
    public String getServiceName() { return serviceName; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getServiceType() { return serviceType; }
    public String getCategory() { return category; }
    public String getVersion() { return version; }
    public String getServiceClass() { return serviceClass; }
    public String getEndpoint() { return endpoint; }
    public int getPort() { return port; }
    public String getProtocol() { return protocol; }
    public String getStatus() { return status; }
    public String getHealthCheckUrl() { return healthCheckUrl; }
    public String getMetricsUrl() { return metricsUrl; }
    public String[] getDependencies() { return dependencies; }
    public Map<String, Object> getMetadata() { return metadata; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public boolean isHealthy() { return healthy && "ACTIVE".equals(status); }
    
    // Setters
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setCategory(String category) { this.category = category; }
    public void setVersion(String version) { this.version = version; }
    public void setServiceClass(String serviceClass) { this.serviceClass = serviceClass; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public void setPort(int port) { this.port = port; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setStatus(String status) { this.status = status; }
    public void setHealthCheckUrl(String healthCheckUrl) { this.healthCheckUrl = healthCheckUrl; }
    public void setMetricsUrl(String metricsUrl) { this.metricsUrl = metricsUrl; }
    public void setDependencies(String[] dependencies) { this.dependencies = dependencies; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    
    // Additional setter methods for ServiceManagementService
    public void setCreatedBy(Long createdBy) { this.metadata.put("createdBy", createdBy); }
    public void setCreatedAt(LocalDateTime createdAt) { this.registeredAt = createdAt; }
    public void setAutoScalingEnabled(boolean autoScalingEnabled) { this.metadata.put("autoScalingEnabled", autoScalingEnabled); }
    public void setMinInstances(int minInstances) { this.metadata.put("minInstances", minInstances); }
    public void setMaxInstances(int maxInstances) { this.metadata.put("maxInstances", maxInstances); }
    public void setDesiredInstances(int desiredInstances) { this.metadata.put("desiredInstances", desiredInstances); }
    public void setHealthCheckInterval(int healthCheckInterval) { this.metadata.put("healthCheckInterval", healthCheckInterval); }
    public void setLoadBalancerType(String loadBalancerType) { this.metadata.put("loadBalancerType", loadBalancerType); }
    
    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }
}

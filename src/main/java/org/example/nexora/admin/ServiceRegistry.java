package org.example.nexora.admin;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service registry entity
 */
@Data
public class ServiceRegistry {
    
    private Long id;
    private String serviceName;
    private String serviceType;
    private String version;
    private String status; // ACTIVE, INACTIVE, MAINTENANCE, ERROR
    private String host;
    private int port;
    private LocalDateTime registeredAt;
    private LocalDateTime lastHeartbeat;
    private Map<String, Object> metadata;
    private boolean healthy;
    
    public ServiceRegistry() {
        this.registeredAt = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
        this.status = "ACTIVE";
        this.healthy = true;
    }
    
    public boolean isHealthy() {
        return healthy && "ACTIVE".equals(status);
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
}

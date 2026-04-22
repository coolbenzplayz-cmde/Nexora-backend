package org.example.nexora.admin.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Service registration request
 */
@Data
public class ServiceRegistrationRequest {
    
    @NotBlank(message = "Service name is required")
    private String serviceName;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private String description;
    private String serviceType;
    private String version;
    private String host;
    private int port;
    private String healthEndpoint;
    private String[] dependencies;
    private String environment; // DEV, STAGING, PROD
    private String category;
    private String serviceClass;
    private String endpoint;
    private int port;
    private String protocol;
    private String healthCheckUrl;
    private String metricsUrl;
    private Map<String, Object> configuration;
    
    public ServiceRegistrationRequest() {
        this.version = "1.0.0";
        this.healthEndpoint = "/actuator/health";
        this.environment = "PROD";
        this.dependencies = new String[0];
        this.port = 8080;
        this.protocol = "HTTP";
        this.healthCheckUrl = "/actuator/health";
        this.configuration = new java.util.HashMap<>();
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getServiceClass() {
        return serviceClass;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }
    
    public String getMetricsUrl() {
        return metricsUrl;
    }
    
    public Map<String, Object> getConfiguration() {
        return configuration;
    }
    
    public String[] getDependencies() {
        return dependencies;
    }
}

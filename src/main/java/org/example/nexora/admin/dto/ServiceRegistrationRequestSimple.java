package org.example.nexora.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Simple service registration request without Lombok
 */
public class ServiceRegistrationRequestSimple {
    
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
    private String protocol;
    private String healthCheckUrl;
    private String metricsUrl;
    private Map<String, Object> configuration;
    private boolean autoScalingEnabled;
    private Integer minInstances;
    private Integer maxInstances;
    private Integer desiredInstances;
    private Integer healthCheckInterval;
    private String loadBalancerType;
    
    public ServiceRegistrationRequestSimple() {
        this.version = "1.0.0";
        this.healthEndpoint = "/actuator/health";
        this.environment = "PROD";
        this.dependencies = new String[0];
        this.port = 8080;
        this.protocol = "HTTP";
        this.healthCheckUrl = "/actuator/health";
        this.configuration = new java.util.HashMap<>();
        this.autoScalingEnabled = false;
        this.minInstances = 1;
        this.maxInstances = 3;
        this.loadBalancerType = "ROUND_ROBIN";
    }
    
    // Getters
    public String getServiceName() { return serviceName; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getServiceType() { return serviceType; }
    public String getVersion() { return version; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getHealthEndpoint() { return healthEndpoint; }
    public String[] getDependencies() { return dependencies; }
    public String getEnvironment() { return environment; }
    public String getCategory() { return category; }
    public String getServiceClass() { return serviceClass; }
    public String getEndpoint() { return endpoint; }
    public String getProtocol() { return protocol; }
    public String getHealthCheckUrl() { return healthCheckUrl; }
    public String getMetricsUrl() { return metricsUrl; }
    public Map<String, Object> getConfiguration() { return configuration; }
    public boolean isAutoScalingEnabled() { return autoScalingEnabled; }
    public Integer getMinInstances() { return minInstances; }
    public Integer getMaxInstances() { return maxInstances; }
    public Integer getDesiredInstances() { return desiredInstances; }
    public Integer getHealthCheckInterval() { return healthCheckInterval; }
    public String getLoadBalancerType() { return loadBalancerType; }
    
    // Setters
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setVersion(String version) { this.version = version; }
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setHealthEndpoint(String healthEndpoint) { this.healthEndpoint = healthEndpoint; }
    public void setDependencies(String[] dependencies) { this.dependencies = dependencies; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setCategory(String category) { this.category = category; }
    public void setServiceClass(String serviceClass) { this.serviceClass = serviceClass; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setHealthCheckUrl(String healthCheckUrl) { this.healthCheckUrl = healthCheckUrl; }
    public void setMetricsUrl(String metricsUrl) { this.metricsUrl = metricsUrl; }
    public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    public void setAutoScalingEnabled(boolean autoScalingEnabled) { this.autoScalingEnabled = autoScalingEnabled; }
    public void setMinInstances(Integer minInstances) { this.minInstances = minInstances; }
    public void setMaxInstances(Integer maxInstances) { this.maxInstances = maxInstances; }
    public void setDesiredInstances(Integer desiredInstances) { this.desiredInstances = desiredInstances; }
    public void setHealthCheckInterval(Integer healthCheckInterval) { this.healthCheckInterval = healthCheckInterval; }
    public void setLoadBalancerType(String loadBalancerType) { this.loadBalancerType = loadBalancerType; }
}

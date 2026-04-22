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
}

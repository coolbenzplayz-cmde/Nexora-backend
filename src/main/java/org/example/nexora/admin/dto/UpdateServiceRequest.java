package org.example.nexora.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.nexora.admin.ServiceStatus;

@Data
public class UpdateServiceRequest {
    
    @Size(max = 100, message = "Service name must be less than 100 characters")
    private String name;
    
    private String description;
    
    @Size(max = 50, message = "Category must be less than 50 characters")
    private String category;
    
    @Size(max = 255, message = "API endpoint must be less than 255 characters")
    private String apiEndpoint;
    
    @Size(max = 20, message = "Version must be less than 20 characters")
    private String version;
    
    private ServiceStatus status;
    
    private Boolean isPublic;
    
    private Boolean requiresAuth;
    
    private Integer rateLimit;
    
    private String config;
    
    @Size(max = 500, message = "Documentation URL must be less than 500 characters")
    private String documentationUrl;
    
    @Size(max = 100, message = "Contact email must be less than 100 characters")
    private String contactEmail;
}

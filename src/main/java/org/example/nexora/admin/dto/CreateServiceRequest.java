package org.example.nexora.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateServiceRequest {
    
    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must be less than 100 characters")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must be less than 50 characters")
    private String category;
    
    @Size(max = 255, message = "API endpoint must be less than 255 characters")
    private String apiEndpoint;
    
    @Size(max = 20, message = "Version must be less than 20 characters")
    private String version = "1.0.0";
    
    private Boolean isPublic = true;
    
    private Boolean requiresAuth = false;
    
    private Integer rateLimit = 1000;
    
    private String config;
    
    @Size(max = 500, message = "Documentation URL must be less than 500 characters")
    private String documentationUrl;
    
    @Size(max = 100, message = "Contact email must be less than 100 characters")
    private String contactEmail;
}

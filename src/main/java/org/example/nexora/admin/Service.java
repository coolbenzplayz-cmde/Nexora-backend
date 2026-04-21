package org.example.nexora.admin;

import jakarta.persistence.*;
import org.example.nexora.common.BaseEntity;

@Entity
@Table(name = "services", indexes = {
        @Index(name = "idx_services_category", columnList = "category"),
        @Index(name = "idx_services_status", columnList = "status"),
        @Index(name = "idx_services_name", columnList = "name")
})
public class Service extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;

    @Column(name = "version", length = 20)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "requires_auth", nullable = false)
    private Boolean requiresAuth = false;

    @Column(name = "rate_limit", columnDefinition = "INTEGER DEFAULT 1000")
    private Integer rateLimit = 1000;

    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    // Default constructor
    public Service() {
    }

    // Full constructor
    public Service(String name, String description, String category, String apiEndpoint, 
                   String version, ServiceStatus status, Boolean isPublic, Boolean requiresAuth, 
                   Integer rateLimit, String config, String documentationUrl, String contactEmail) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.apiEndpoint = apiEndpoint;
        this.version = version;
        this.status = status;
        this.isPublic = isPublic;
        this.requiresAuth = requiresAuth;
        this.rateLimit = rateLimit;
        this.config = config;
        this.documentationUrl = documentationUrl;
        this.contactEmail = contactEmail;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getRequiresAuth() {
        return requiresAuth;
    }

    public void setRequiresAuth(Boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }

    public Integer getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Integer rateLimit) {
        this.rateLimit = rateLimit;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}

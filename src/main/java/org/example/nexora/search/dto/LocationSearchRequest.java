package org.example.nexora.search.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Location search request
 */
@Data
public class LocationSearchRequest {
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private String query;
    private double radius; // in kilometers
    private String[] searchTypes; // USER, BUSINESS, PRODUCT, SERVICE
    private String category;
    private boolean activeOnly = true;
    private int page = 0;
    private int size = 20;
    private String sortBy = "distance";
    private String sortOrder = "asc";
    
    public LocationSearchRequest() {
        this.radius = 10.0;
        this.searchTypes = new String[]{"USER", "BUSINESS", "PRODUCT", "SERVICE"};
    }
}

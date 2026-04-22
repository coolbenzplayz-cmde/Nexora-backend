package org.example.nexora.search.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * Product search request
 */
@Data
public class ProductSearchRequest {
    
    @NotBlank(message = "Query is required")
    private String query;
    
    private String category;
    private String[] tags;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String condition; // NEW, USED, REFURBISHED
    private boolean availableOnly = true;
    private String location;
    private int page = 0;
    private int size = 20;
    private String sortBy = "relevance";
    private String sortOrder = "desc";
    
    public ProductSearchRequest() {
        this.condition = "NEW";
    }
}

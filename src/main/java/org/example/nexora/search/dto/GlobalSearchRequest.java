package org.example.nexora.search.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Global search request
 */
@Data
public class GlobalSearchRequest {
    
    @NotBlank(message = "Query is required")
    private String query;
    
    private String[] contentTypes; // USER, POST, PRODUCT, ARTICLE, VIDEO
    private String category;
    private String tags;
    private int page = 0;
    private int size = 20;
    private String sortBy = "relevance"; // relevance, date, popularity
    private String sortOrder = "desc";
    
    public GlobalSearchRequest() {
        this.contentTypes = new String[]{"USER", "POST", "PRODUCT", "ARTICLE", "VIDEO"};
    }
}

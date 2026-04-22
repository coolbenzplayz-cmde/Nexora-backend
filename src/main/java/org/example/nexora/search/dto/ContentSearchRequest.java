package org.example.nexora.search.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Content search request
 */
@Data
public class ContentSearchRequest {
    
    @NotBlank(message = "Query is required")
    private String query;
    
    private String[] contentTypes; // POST, ARTICLE, VIDEO, IMAGE
    private String category;
    private String[] tags;
    private Long authorId;
    private String dateRange; // today, week, month, year, all
    private boolean publishedOnly = true;
    private int page = 0;
    private int size = 20;
    private String sortBy = "relevance";
    private String sortOrder = "desc";
    
    public ContentSearchRequest() {
        this.contentTypes = new String[]{"POST", "ARTICLE", "VIDEO", "IMAGE"};
    }
}

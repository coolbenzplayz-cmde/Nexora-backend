package org.example.nexora.search.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Index request for search indexing
 */
@Data
public class IndexRequest {
    
    @NotBlank(message = "Content ID is required")
    private String contentId;
    
    @NotBlank(message = "Content type is required")
    private String contentType; // USER, POST, PRODUCT, ARTICLE, VIDEO
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String content;
    private String tags;
    private Long authorId;
    private String category;
    private double relevance;
    
    public IndexRequest() {
        this.relevance = 1.0;
    }
}

package org.example.nexora.search.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * User search request
 */
@Data
public class UserSearchRequest {
    
    @NotBlank(message = "Query is required")
    private String query;
    
    private String[] fields; // username, email, firstName, lastName, bio
    private String location;
    private String[] interests;
    private boolean activeOnly = true;
    private int page = 0;
    private int size = 20;
    private String sortBy = "relevance";
    private String sortOrder = "desc";
    
    public UserSearchRequest() {
        this.fields = new String[]{"username", "firstName", "lastName", "bio"};
    }
}

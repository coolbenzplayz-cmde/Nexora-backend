package org.example.nexora.search.dto;

import lombok.Data;
import java.util.List;

/**
 * User search result
 */
@Data
public class UserSearchResult {
    
    private String query;
    private long totalResults;
    private int page;
    private int size;
    private int totalPages;
    private List<UserSearchHit> hits;
    private long searchTime;
    
    public UserSearchResult() {
        this.page = 0;
        this.size = 20;
        this.searchTime = 0;
    }
    
    @Data
    public static class UserSearchHit {
        private Long userId;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private String bio;
        private String avatarUrl;
        private String location;
        private boolean active;
        private double score;
        private String[] interests;
        private int followersCount;
        private int followingCount;
    }
}

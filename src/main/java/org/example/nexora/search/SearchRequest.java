package org.example.nexora.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexora.video.Video;
import org.example.nexora.user.User;
import org.example.nexora.social.Comment;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    private String query;
    private Long userId; // For personalization
    private List<Video> availableVideos;
    private List<User> availableUsers;
    private List<Comment> availableComments;
    
    // Search filters
    private boolean includeVideos = true;
    private boolean includeUsers = true;
    private boolean includeComments = false;
    
    // Video-specific filters
    private Integer minDuration;
    private Integer maxDuration;
    private boolean highQualityOnly = false;
    private boolean verifiedCreatorsOnly = false;
    private String category;
    private String language;
    
    // User-specific filters
    private boolean verifiedUsersOnly = false;
    private boolean activeUsersOnly = true;
    private String location;
    
    // Sorting options
    private SortOption sortBy = SortOption.RELEVANCE;
    private SortOrder sortOrder = SortOrder.DESCENDING;
    
    // Pagination
    private int page = 0;
    private int size = 20;
    
    // Advanced options
    private boolean enableFuzzySearch = true;
    private boolean enableSemanticSearch = false;
    private double minRelevanceScore = 0.1;
    private boolean enablePersonalization = true;

    public enum SortOption {
        RELEVANCE, DATE, VIEWS, LIKES, ENGAGEMENT, POPULARITY
    }

    public enum SortOrder {
        ASCENDING, DESCENDING
    }
}

package org.example.nexora.search.dto;

import lombok.Data;
import java.util.List;

/**
 * Content search result
 */
@Data
public class ContentSearchResult {
    
    private String query;
    private long totalResults;
    private int page;
    private int size;
    private int totalPages;
    private List<ContentSearchHit> hits;
    private long searchTime;
    
    public ContentSearchResult() {
        this.page = 0;
        this.size = 20;
        this.searchTime = 0;
    }
    
    @Data
    public static class ContentSearchHit {
        private String contentId;
        private String contentType;
        private String title;
        private String description;
        private String content;
        private String url;
        private Long authorId;
        private String authorName;
        private String category;
        private String[] tags;
        private String thumbnailUrl;
        private boolean published;
        private String publishedAt;
        private double score;
        private int viewsCount;
        private int likesCount;
        private int commentsCount;
    }
}

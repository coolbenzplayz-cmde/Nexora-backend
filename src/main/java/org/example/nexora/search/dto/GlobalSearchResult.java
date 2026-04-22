package org.example.nexora.search.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Global search result
 */
@Data
public class GlobalSearchResult {
    
    private String query;
    private long totalResults;
    private int page;
    private int size;
    private int totalPages;
    private List<SearchHit> hits;
    private Map<String, Long> aggregations;
    private long searchTime;
    
    public GlobalSearchResult() {
        this.page = 0;
        this.size = 20;
        this.searchTime = 0;
    }
    
    @Data
    public static class SearchHit {
        private String id;
        private String type;
        private String title;
        private String description;
        private String url;
        private double score;
        private Map<String, Object> source;
        private String highlight;
    }
}

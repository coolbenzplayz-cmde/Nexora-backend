package org.example.nexora.search.dto;

import lombok.Data;
import java.util.List;

/**
 * Location search result
 */
@Data
public class LocationSearchResult {
    
    private String location;
    private double radius;
    private long totalResults;
    private int page;
    private int size;
    private int totalPages;
    private List<LocationSearchHit> hits;
    private long searchTime;
    
    public LocationSearchResult() {
        this.page = 0;
        this.size = 20;
        this.radius = 10.0;
        this.searchTime = 0;
    }
    
    @Data
    public static class LocationSearchHit {
        private String id;
        private String type; // USER, BUSINESS, PRODUCT, SERVICE
        private String title;
        private String description;
        private String address;
        private double distance; // in kilometers
        private String latitude;
        private String longitude;
        private String category;
        private boolean active;
        private double rating;
        private int reviewsCount;
        private String imageUrl;
        private double score;
        private String contactInfo;
    }
}

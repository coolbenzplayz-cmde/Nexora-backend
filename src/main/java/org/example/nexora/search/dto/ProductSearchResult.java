package org.example.nexora.search.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * Product search result
 */
@Data
public class ProductSearchResult {
    
    private String query;
    private long totalResults;
    private int page;
    private int size;
    private int totalPages;
    private List<ProductSearchHit> hits;
    private long searchTime;
    
    public ProductSearchResult() {
        this.page = 0;
        this.size = 20;
        this.searchTime = 0;
    }
    
    @Data
    public static class ProductSearchHit {
        private String productId;
        private String title;
        private String description;
        private BigDecimal price;
        private String currency;
        private String category;
        private String[] tags;
        private String condition;
        private boolean available;
        private int stockQuantity;
        private String location;
        private String sellerId;
        private String sellerName;
        private String imageUrl;
        private double rating;
        private int reviewsCount;
        private double score;
        private String createdAt;
    }
}

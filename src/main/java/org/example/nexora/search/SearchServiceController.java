package org.example.nexora.search;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.example.nexora.search.dto.IndexRequest;
import org.example.nexora.search.dto.GlobalSearchRequest;
import org.example.nexora.search.dto.UserSearchRequest;
import org.example.nexora.search.dto.ContentSearchRequest;
import org.example.nexora.search.dto.ProductSearchRequest;
import org.example.nexora.search.dto.LocationSearchRequest;

/**
 * Search Service Controller - Handles search endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchServiceController {

    private final SearchEngineService searchEngineService;
    private final SearchAnalyticsService searchAnalyticsService;
    private final SearchSuggestionService searchSuggestionService;
    private final SearchIndexingService searchIndexingService;

    /**
     * Global search across all content types
     */
    @GetMapping
    public ResponseEntity<GlobalSearchResponse> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String sort,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Global search: {} by user: {}", query, userId);

        try {
            GlobalSearchRequest request = new GlobalSearchRequest();
            request.setQuery(query);
            request.setPage(page);
            request.setSize(size);
            request.setFilters(filters);
            request.setSort(sort);
            request.setUserId(userId);

            GlobalSearchResult result = searchEngineService.globalSearch(request);

            // Log search analytics
            searchAnalyticsService.logSearch(userId, query, result.getTotal());

            GlobalSearchResponse response = new GlobalSearchResponse();
            response.setSuccess(true);
            response.setResults(result.getResults());
            response.setTotal(result.getTotal());
            response.setPage(result.getPage());
            response.setSize(result.getSize());
            response.setSearchTime(result.getSearchTime());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Global search failed", e);
            return ResponseEntity.badRequest()
                    .body(GlobalSearchResponse.failure("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Search users
     */
    @GetMapping("/users")
    public ResponseEntity<UserSearchResponse> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filters,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("User search: {} by user: {}", query, userId);

        try {
            UserSearchRequest request = new UserSearchRequest();
            request.setQuery(query);
            request.setPage(page);
            request.setSize(size);
            request.setFilters(filters);
            request.setUserId(userId);

            UserSearchResult result = searchEngineService.searchUsers(request);

            UserSearchResponse response = new UserSearchResponse();
            response.setSuccess(true);
            response.setUsers(result.getUsers());
            response.setTotal(result.getTotal());
            response.setPage(result.getPage());
            response.setSize(result.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("User search failed", e);
            return ResponseEntity.badRequest()
                    .body(UserSearchResponse.failure("User search failed: " + e.getMessage()));
        }
    }

    /**
     * Search content (videos, posts, media)
     */
    @GetMapping("/content")
    public ResponseEntity<ContentSearchResponse> searchContent(
            @RequestParam String query,
            @RequestParam(required = false) String contentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String sort,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Content search: {} type: {} by user: {}", query, contentType, userId);

        try {
            ContentSearchRequest request = new ContentSearchRequest();
            request.setQuery(query);
            request.setContentType(contentType);
            request.setPage(page);
            request.setSize(size);
            request.setFilters(filters);
            request.setSort(sort);
            request.setUserId(userId);

            ContentSearchResult result = searchEngineService.searchContent(request);

            ContentSearchResponse response = new ContentSearchResponse();
            response.setSuccess(true);
            response.setContent(result.getContent());
            response.setTotal(result.getTotal());
            response.setPage(result.getPage());
            response.setSize(result.getSize());
            response.setAggregations(result.getAggregations());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Content search failed", e);
            return ResponseEntity.badRequest()
                    .body(ContentSearchResponse.failure("Content search failed: " + e.getMessage()));
        }
    }

    /**
     * Search products (marketplace)
     */
    @GetMapping("/products")
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Product search: {} category: {} by user: {}", query, category, userId);

        try {
            ProductSearchRequest request = new ProductSearchRequest();
            request.setQuery(query);
            request.setCategory(category);
            request.setMinPrice(minPrice);
            request.setMaxPrice(maxPrice);
            request.setLocation(location);
            request.setPage(page);
            request.setSize(size);
            request.setSort(sort);
            request.setUserId(userId);

            ProductSearchResult result = searchEngineService.searchProducts(request);

            ProductSearchResponse response = new ProductSearchResponse();
            response.setSuccess(true);
            response.setProducts(result.getProducts());
            response.setTotal(result.getTotal());
            response.setPage(result.getPage());
            response.setSize(result.getSize());
            response.setFilters(result.getFilters());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Product search failed", e);
            return ResponseEntity.badRequest()
                    .body(ProductSearchResponse.failure("Product search failed: " + e.getMessage()));
        }
    }

    /**
     * Location-based search
     */
    @GetMapping("/location")
    public ResponseEntity<LocationSearchResponse> searchByLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "10") Double radius,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Location search: lat:{}, lon:{}, radius:{}km by user: {}", latitude, longitude, radius, userId);

        try {
            LocationSearchRequest request = new LocationSearchRequest();
            request.setLatitude(latitude);
            request.setLongitude(longitude);
            request.setRadius(radius);
            request.setType(type);
            request.setPage(page);
            request.setSize(size);
            request.setUserId(userId);

            LocationSearchResult result = searchEngineService.searchByLocation(request);

            LocationSearchResponse response = new LocationSearchResponse();
            response.setSuccess(true);
            response.setResults(result.getResults());
            response.setTotal(result.getTotal());
            response.setPage(result.getPage());
            response.setSize(result.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Location search failed", e);
            return ResponseEntity.badRequest()
                    .body(LocationSearchResponse.failure("Location search failed: " + e.getMessage()));
        }
    }

    /**
     * Get search suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionsResponse> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Getting search suggestions: {} type: {} for user: {}", query, type, userId);

        try {
            List<String> suggestions = searchSuggestionService.getSuggestions(query, type, limit, userId);

            SearchSuggestionsResponse response = new SearchSuggestionsResponse();
            response.setSuccess(true);
            response.setSuggestions(suggestions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get search suggestions", e);
            return ResponseEntity.badRequest()
                    .body(SearchSuggestionsResponse.failure("Failed to get suggestions: " + e.getMessage()));
        }
    }

    /**
     * Get trending searches
     */
    @GetMapping("/trending")
    public ResponseEntity<TrendingSearchResponse> getTrendingSearches(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Getting trending searches category: {} for user: {}", category, userId);

        try {
            List<TrendingSearch> trending = searchAnalyticsService.getTrendingSearches(category, limit, userId);

            TrendingSearchResponse response = new TrendingSearchResponse();
            response.setSuccess(true);
            response.setTrending(trending);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get trending searches", e);
            return ResponseEntity.badRequest()
                    .body(TrendingSearchResponse.failure("Failed to get trending searches: " + e.getMessage()));
        }
    }

    /**
     * Search analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<SearchAnalyticsResponse> getSearchAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String category,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Getting search analytics period: {} category: {} for user: {}", period, category, userId);

        try {
            SearchAnalytics analytics = searchAnalyticsService.getSearchAnalytics(period, category, userId);

            SearchAnalyticsResponse response = new SearchAnalyticsResponse();
            response.setSuccess(true);
            response.setAnalytics(analytics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get search analytics", e);
            return ResponseEntity.badRequest()
                    .body(SearchAnalyticsResponse.failure("Failed to get analytics: " + e.getMessage()));
        }
    }

    /**
     * Index content (admin endpoint)
     */
    @PostMapping("/index")
    public ResponseEntity<IndexResponse> indexContent(
            @Valid @RequestBody IndexRequest request,
            @RequestHeader("X-User-ID") Long userId) {
        log.info("Indexing content: {} by user: {}", request.getType(), userId);

        try {
            // Check admin permissions
            if (!hasAdminAccess(userId)) {
                return ResponseEntity.badRequest()
                        .body(IndexResponse.failure("Access denied"));
            }

            IndexResult result = searchIndexingService.indexContent(request);

            IndexResponse response = new IndexResponse();
            response.setSuccess(true);
            response.setIndexedCount(result.getIndexedCount());
            response.setProcessingTime(result.getProcessingTime());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to index content", e);
            return ResponseEntity.badRequest()
                    .body(IndexResponse.failure("Indexing failed: " + e.getMessage()));
        }
    }

    // Request classes
    @Data
    public static class GlobalSearchRequest {
        private String query;
        private int page;
        private int size;
        private String filters;
        private String sort;
        private Long userId;
    }

    @Data
    public static class UserSearchRequest {
        private String query;
        private int page;
        private int size;
        private String filters;
        private Long userId;
    }

    @Data
    public static class ContentSearchRequest {
        private String query;
        private String contentType;
        private int page;
        private int size;
        private String filters;
        private String sort;
        private Long userId;
    }

    @Data
    public static class ProductSearchRequest {
        private String query;
        private String category;
        private Double minPrice;
        private Double maxPrice;
        private String location;
        private int page;
        private int size;
        private String sort;
        private Long userId;
    }

    @Data
    public static class LocationSearchRequest {
        private Double latitude;
        private Double longitude;
        private Double radius;
        private String type;
        private int page;
        private int size;
        private Long userId;
    }

    @Data
    public static class IndexRequest {
        private String type;
        private String contentId;
        private java.util.Map<String, Object> data;
        private boolean update;
    }

    // Response classes
    @Data
    public static class GlobalSearchResponse {
        private boolean success;
        private java.util.List<SearchResult> results;
        private long total;
        private int page;
        private int size;
        private long searchTime;
        private String error;

        public static GlobalSearchResponse failure(String error) {
            GlobalSearchResponse response = new GlobalSearchResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class UserSearchResponse {
        private boolean success;
        private java.util.List<UserSearchResult> users;
        private long total;
        private int page;
        private int size;
        private String error;

        public static UserSearchResponse failure(String error) {
            UserSearchResponse response = new UserSearchResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class ContentSearchResponse {
        private boolean success;
        private java.util.List<ContentSearchResult> content;
        private long total;
        private int page;
        private int size;
        private java.util.Map<String, Object> aggregations;
        private String error;

        public static ContentSearchResponse failure(String error) {
            ContentSearchResponse response = new ContentSearchResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class ProductSearchResponse {
        private boolean success;
        private java.util.List<ProductSearchResult> products;
        private long total;
        private int page;
        private int size;
        private java.util.Map<String, Object> filters;
        private String error;

        public static ProductSearchResponse failure(String error) {
            ProductSearchResponse response = new ProductSearchResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class LocationSearchResponse {
        private boolean success;
        private java.util.List<LocationSearchResult> results;
        private long total;
        private int page;
        private int size;
        private String error;

        public static LocationSearchResponse failure(String error) {
            LocationSearchResponse response = new LocationSearchResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class SearchSuggestionsResponse {
        private boolean success;
        private java.util.List<String> suggestions;
        private String error;

        public static SearchSuggestionsResponse failure(String error) {
            SearchSuggestionsResponse response = new SearchSuggestionsResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class TrendingSearchResponse {
        private boolean success;
        private java.util.List<TrendingSearch> trending;
        private String error;

        public static TrendingSearchResponse failure(String error) {
            TrendingSearchResponse response = new TrendingSearchResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class SearchAnalyticsResponse {
        private boolean success;
        private SearchAnalytics analytics;
        private String error;

        public static SearchAnalyticsResponse failure(String error) {
            SearchAnalyticsResponse response = new SearchAnalyticsResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    @Data
    public static class IndexResponse {
        private boolean success;
        private int indexedCount;
        private long processingTime;
        private String error;

        public static IndexResponse failure(String error) {
            IndexResponse response = new IndexResponse();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }

    // Helper method
    private boolean hasAdminAccess(Long userId) {
        // Simplified admin check
        return userId != null && userId == 1L;
    }
}

// Service placeholders
class SearchEngineService {
    public GlobalSearchResult globalSearch(GlobalSearchRequest request) { return new GlobalSearchResult(); }
    public UserSearchResult searchUsers(UserSearchRequest request) { return new UserSearchResult(); }
    public ContentSearchResult searchContent(ContentSearchRequest request) { return new ContentSearchResult(); }
    public ProductSearchResult searchProducts(ProductSearchRequest request) { return new ProductSearchResult(); }
    public LocationSearchResult searchByLocation(LocationSearchRequest request) { return new LocationSearchResult(); }
}

class SearchAnalyticsService {
    public void logSearch(Long userId, String query, long total) {}
    public java.util.List<TrendingSearch> getTrendingSearches(String category, int limit, Long userId) { return new java.util.ArrayList<>(); }
    public SearchAnalytics getSearchAnalytics(String period, String category, Long userId) { return new SearchAnalytics(); }
}

class SearchSuggestionService {
    public java.util.List<String> getSuggestions(String query, String type, int limit, Long userId) { return new java.util.ArrayList<>(); }
}

class SearchIndexingService {
    public IndexResult indexContent(IndexRequest request) { return new IndexResult(); }
}

// Data classes
class SearchResult {
    private String type;
    private String id;
    private String title;
    private String description;
    private java.util.Map<String, Object> metadata;
    private double score;
}

class GlobalSearchResult {
    private java.util.List<SearchResult> results;
    private long total;
    private int page;
    private int size;
    private long searchTime;
}

class UserSearchResult {
    private Long userId;
    private String name;
    private String email;
    private String avatar;
    private java.util.Map<String, Object> profile;
    private double score;
}

class ContentSearchResult {
    private String contentId;
    private String title;
    private String description;
    private String contentType;
    private String thumbnail;
    private java.util.Map<String, Object> metadata;
    private double score;
}

class ProductSearchResult {
    private String productId;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String location;
    private String image;
    private double score;
}

class LocationSearchResult {
    private String id;
    private String type;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private java.util.Map<String, Object> metadata;
}

class UserSearchResult {
    private java.util.List<UserSearchResultItem> users;
    private long total;
    private int page;
    private int size;
}

class ContentSearchResult {
    private java.util.List<ContentSearchResultItem> content;
    private long total;
    private int page;
    private int size;
    private java.util.Map<String, Object> aggregations;
}

class ProductSearchResult {
    private java.util.List<ProductSearchResultItem> products;
    private long total;
    private int page;
    private int size;
    private java.util.Map<String, Object> filters;
}

class LocationSearchResult {
    private java.util.List<LocationSearchResultItem> results;
    private long total;
    private int page;
    private int size;
}

class UserSearchResultItem {
    private String id;
    private String username;
    private String email;
    private String displayName;
    private String profilePicture;
    private java.util.Map<String, Object> metadata;
}

class ContentSearchResultItem {
    private String id;
    private String title;
    private String description;
    private String contentType;
    private String author;
    private java.time.LocalDateTime createdAt;
    private java.util.Map<String, Object> metadata;
}

class ProductSearchResultItem {
    private String id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private String currency;
    private java.util.Map<String, Object> metadata;
}

class LocationSearchResultItem {
    private String id;
    private String name;
    private String type;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private java.util.Map<String, Object> metadata;
}

class TrendingSearch {
    private String query;
    private long count;
    private String category;
    private java.time.LocalDateTime timestamp;
}

class SearchAnalytics {
    private java.util.Map<String, Object> metrics;
    private java.util.List<TrendingSearch> topQueries;
    private java.time.LocalDateTime generatedAt;
}

class IndexResult {
    private int indexedCount;
    private long processingTime;
}

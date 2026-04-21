package org.example.nexora.search;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.video.Video;
import org.example.nexora.user.User;
import org.example.nexora.social.Comment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Search Engine with full-text search, semantic search, and personalized ranking
 */
@Slf4j
public class SearchEngine {

    private static final int MAX_RESULTS = 50;
    private static final double MIN_RELEVANCE_SCORE = 0.1;

    /**
     * Perform comprehensive search across all content types
     */
    public SearchResult search(String query, SearchRequest searchRequest) {
        log.info("Performing search for query: {}", query);

        SearchResult result = new SearchResult();
        result.setQuery(query);
        result.setTimestamp(System.currentTimeMillis());

        // Search different content types
        if (searchRequest.isIncludeVideos()) {
            List<VideoSearchResult> videoResults = searchVideos(query, searchRequest);
            result.setVideos(videoResults);
        }

        if (searchRequest.isIncludeUsers()) {
            List<UserSearchResult> userResults = searchUsers(query, searchRequest);
            result.setUsers(userResults);
        }

        if (searchRequest.isIncludeComments()) {
            List<CommentSearchResult> commentResults = searchComments(query, searchRequest);
            result.setComments(commentResults);
        }

        // Calculate overall statistics
        result.setTotalResults(
                (result.getVideos() != null ? result.getVideos().size() : 0) +
                (result.getUsers() != null ? result.getUsers().size() : 0) +
                (result.getComments() != null ? result.getComments().size() : 0)
        );

        return result;
    }

    /**
     * Search videos with advanced ranking
     */
    public List<VideoSearchResult> searchVideos(String query, SearchRequest searchRequest) {
        List<Video> candidateVideos = searchRequest.getAvailableVideos();
        List<VideoSearchResult> results = new ArrayList<>();

        String normalizedQuery = query.toLowerCase().trim();

        for (Video video : candidateVideos) {
            if (shouldIncludeVideo(video, searchRequest)) {
                double relevanceScore = calculateVideoRelevanceScore(video, normalizedQuery, searchRequest);
                
                if (relevanceScore >= MIN_RELEVANCE_SCORE) {
                    VideoSearchResult result = new VideoSearchResult();
                    result.setVideo(video);
                    result.setRelevanceScore(relevanceScore);
                    result.setMatchReasons(getVideoMatchReasons(video, normalizedQuery));
                    result.setHighlights(highlightVideoContent(video, normalizedQuery));
                    results.add(result);
                }
            }
        }

        // Sort by relevance and apply filters
        List<VideoSearchResult> sortedResults = results.stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());

        // Apply additional filtering
        return applyVideoFilters(sortedResults, searchRequest);
    }

    /**
     * Search users with profile matching
     */
    public List<UserSearchResult> searchUsers(String query, SearchRequest searchRequest) {
        List<User> candidateUsers = searchRequest.getAvailableUsers();
        List<UserSearchResult> results = new ArrayList<>();

        String normalizedQuery = query.toLowerCase().trim();

        for (User user : candidateUsers) {
            if (shouldIncludeUser(user, searchRequest)) {
                double relevanceScore = calculateUserRelevanceScore(user, normalizedQuery, searchRequest);
                
                if (relevanceScore >= MIN_RELEVANCE_SCORE) {
                    UserSearchResult result = new UserSearchResult();
                    result.setUser(user);
                    result.setRelevanceScore(relevanceScore);
                    result.setMatchReasons(getUserMatchReasons(user, normalizedQuery));
                    result.setHighlights(highlightUserProfile(user, normalizedQuery));
                    results.add(result);
                }
            }
        }

        return results.stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    /**
     * Search comments with context
     */
    public List<CommentSearchResult> searchComments(String query, SearchRequest searchRequest) {
        List<Comment> candidateComments = searchRequest.getAvailableComments();
        List<CommentSearchResult> results = new ArrayList<>();

        String normalizedQuery = query.toLowerCase().trim();

        for (Comment comment : candidateComments) {
            if (shouldIncludeComment(comment, searchRequest)) {
                double relevanceScore = calculateCommentRelevanceScore(comment, normalizedQuery, searchRequest);
                
                if (relevanceScore >= MIN_RELEVANCE_SCORE) {
                    CommentSearchResult result = new CommentSearchResult();
                    result.setComment(comment);
                    result.setRelevanceScore(relevanceScore);
                    result.setMatchReasons(getCommentMatchReasons(comment, normalizedQuery));
                    result.setHighlights(highlightCommentContent(comment, normalizedQuery));
                    results.add(result);
                }
            }
        }

        return results.stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    /**
     * Calculate video relevance score using multiple factors
     */
    private double calculateVideoRelevanceScore(Video video, String query, SearchRequest searchRequest) {
        double score = 0.0;
        int factors = 0;

        // Title matching (highest weight)
        if (video.getTitle() != null) {
            double titleScore = calculateTextMatchScore(video.getTitle().toLowerCase(), query);
            score += titleScore * 3.0;
            factors++;
        }

        // Description matching
        if (video.getDescription() != null) {
            double descScore = calculateTextMatchScore(video.getDescription().toLowerCase(), query);
            score += descScore * 2.0;
            factors++;
        }

        // Engagement boost
        double engagementBoost = Math.log10(video.getEngagementScore() + 1) / 10.0;
        score += engagementBoost;
        factors++;

        // Recency boost
        double recencyBoost = calculateRecencyBoost(video.getCreatedAt());
        score += recencyBoost;
        factors++;

        // Quality boost
        double qualityBoost = video.getThumbnailUrl() != null ? 0.2 : 0.0;
        score += qualityBoost;
        factors++;

        // Creator verification boost
        if (video.getUser() != null && video.getUser().getIsCreatorVerified()) {
            score += 0.3;
            factors++;
        }

        // Personalization boost
        if (searchRequest.getUserId() != null) {
            double personalizationBoost = calculatePersonalizationBoost(video, searchRequest.getUserId());
            score += personalizationBoost;
            factors++;
        }

        return factors > 0 ? score / factors : 0.0;
    }

    /**
     * Calculate user relevance score
     */
    private double calculateUserRelevanceScore(User user, String query, SearchRequest searchRequest) {
        double score = 0.0;
        int factors = 0;

        // Username matching
        if (user.getUsername() != null) {
            double usernameScore = calculateTextMatchScore(user.getUsername().toLowerCase(), query);
            score += usernameScore * 3.0;
            factors++;
        }

        // Name matching
        String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
        double nameScore = calculateTextMatchScore(fullName, query);
        score += nameScore * 2.5;
        factors++;

        // Bio matching
        if (user.getBio() != null) {
            double bioScore = calculateTextMatchScore(user.getBio().toLowerCase(), query);
            score += bioScore * 1.5;
            factors++;
        }

        // Location matching
        if (user.getLocation() != null) {
            double locationScore = calculateTextMatchScore(user.getLocation().toLowerCase(), query);
            score += locationScore * 1.0;
            factors++;
        }

        // Popularity boost
        double popularityBoost = Math.log10(user.getFollowersCount() + 1) / 10.0;
        score += popularityBoost;
        factors++;

        // Verification boost
        if (user.getIsCreatorVerified()) {
            score += 0.4;
            factors++;
        }

        // Activity boost
        double activityBoost = user.getLastLogin() != null ? 
                Math.max(0, 1.0 - (System.currentTimeMillis() - user.getLastLogin().getTime()) / (30L * 24 * 60 * 60 * 1000)) : 0.0;
        score += activityBoost * 0.3;
        factors++;

        return factors > 0 ? score / factors : 0.0;
    }

    /**
     * Calculate comment relevance score
     */
    private double calculateCommentRelevanceScore(Comment comment, String query, SearchRequest searchRequest) {
        double score = 0.0;
        int factors = 0;

        // Content matching
        if (comment.getContent() != null) {
            double contentScore = calculateTextMatchScore(comment.getContent().toLowerCase(), query);
            score += contentScore * 3.0;
            factors++;
        }

        // Engagement boost
        double engagementBoost = Math.log10(comment.getLikes() + 1) / 10.0;
        score += engagementBoost;
        factors++;

        // Recency boost
        double recencyBoost = calculateRecencyBoost(comment.getCreatedAt());
        score += recencyBoost;
        factors++;

        return factors > 0 ? score / factors : 0.0;
    }

    /**
     * Calculate text match score using exact match, partial match, and word matching
     */
    private double calculateTextMatchScore(String text, String query) {
        if (text.isEmpty() || query.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;

        // Exact match bonus
        if (text.contains(query)) {
            score += 1.0;
        }

        // Word matching
        String[] queryWords = query.split("\\s+");
        String[] textWords = text.split("\\s+");
        
        int matchedWords = 0;
        for (String queryWord : queryWords) {
            for (String textWord : textWords) {
                if (textWord.contains(queryWord) || queryWord.contains(textWord)) {
                    matchedWords++;
                    break;
                }
            }
        }

        if (queryWords.length > 0) {
            score += (double) matchedWords / queryWords.length * 0.8;
        }

        // Levenshtein distance for fuzzy matching (simplified)
        double fuzzyScore = calculateFuzzyMatchScore(text, query);
        score += fuzzyScore * 0.3;

        return Math.min(score, 1.0);
    }

    /**
     * Simple fuzzy matching score
     */
    private double calculateFuzzyMatchScore(String text, String query) {
        if (text.length() == 0 || query.length() == 0) {
            return 0.0;
        }

        int maxDistance = Math.max(text.length(), query.length());
        int distance = calculateLevenshteinDistance(text, query);
        
        return 1.0 - (double) distance / maxDistance;
    }

    /**
     * Calculate Levenshtein distance
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Calculate recency boost for newer content
     */
    private double calculateRecencyBoost(LocalDateTime createdAt) {
        long hoursOld = java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
        
        if (hoursOld < 1) return 0.3;
        if (hoursOld < 24) return 0.2;
        if (hoursOld < 168) return 0.1;
        if (hoursOld < 720) return 0.05;
        
        return 0.0;
    }

    /**
     * Calculate personalization boost based on user preferences
     */
    private double calculatePersonalizationBoost(Video video, Long userId) {
        // This would integrate with user preferences and behavior
        // For now, return a modest boost
        return 0.1;
    }

    /**
     * Apply additional filters to video results
     */
    private List<VideoSearchResult> applyVideoFilters(List<VideoSearchResult> results, SearchRequest searchRequest) {
        return results.stream()
                .filter(result -> {
                    Video video = result.getVideo();
                    
                    // Duration filter
                    if (searchRequest.getMinDuration() != null && video.getDurationSeconds() != null) {
                        if (video.getDurationSeconds() < searchRequest.getMinDuration()) {
                            return false;
                        }
                    }
                    
                    if (searchRequest.getMaxDuration() != null && video.getDurationSeconds() != null) {
                        if (video.getDurationSeconds() > searchRequest.getMaxDuration()) {
                            return false;
                        }
                    }
                    
                    // Quality filter
                    if (searchRequest.isHighQualityOnly() && video.getThumbnailUrl() == null) {
                        return false;
                    }
                    
                    // Verified creators only
                    if (searchRequest.isVerifiedCreatorsOnly() && 
                        (video.getUser() == null || !video.getUser().getIsCreatorVerified())) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private boolean shouldIncludeVideo(Video video, SearchRequest searchRequest) {
        return video.getStatus() == Video.VideoStatus.PUBLISHED && 
               !video.getContentModerationFlagged() &&
               !video.getIsPrivate();
    }

    private boolean shouldIncludeUser(User user, SearchRequest searchRequest) {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }

    private boolean shouldIncludeComment(Comment comment, SearchRequest searchRequest) {
        return !comment.getIsDeleted() && 
               !comment.getContentModerationFlagged();
    }

    private List<String> getVideoMatchReasons(Video video, String query) {
        List<String> reasons = new ArrayList<>();
        
        if (video.getTitle() != null && video.getTitle().toLowerCase().contains(query)) {
            reasons.add("Title match");
        }
        if (video.getDescription() != null && video.getDescription().toLowerCase().contains(query)) {
            reasons.add("Description match");
        }
        if (video.getEngagementScore() > 5.0) {
            reasons.add("High engagement");
        }
        
        return reasons;
    }

    private List<String> getUserMatchReasons(User user, String query) {
        List<String> reasons = new ArrayList<>();
        
        if (user.getUsername() != null && user.getUsername().toLowerCase().contains(query)) {
            reasons.add("Username match");
        }
        if (user.getIsCreatorVerified()) {
            reasons.add("Verified creator");
        }
        if (user.getFollowersCount() > 10000) {
            reasons.add("Popular creator");
        }
        
        return reasons;
    }

    private List<String> getCommentMatchReasons(Comment comment, String query) {
        List<String> reasons = new ArrayList<>();
        
        if (comment.getContent() != null && comment.getContent().toLowerCase().contains(query)) {
            reasons.add("Content match");
        }
        if (comment.getLikes() > 10) {
            reasons.add("Popular comment");
        }
        
        return reasons;
    }

    private Map<String, String> highlightVideoContent(Video video, String query) {
        Map<String, String> highlights = new HashMap<>();
        
        if (video.getTitle() != null && video.getTitle().toLowerCase().contains(query)) {
            highlights.put("title", highlightText(video.getTitle(), query));
        }
        if (video.getDescription() != null && video.getDescription().toLowerCase().contains(query)) {
            highlights.put("description", highlightText(video.getDescription(), query));
        }
        
        return highlights;
    }

    private Map<String, String> highlightUserProfile(User user, String query) {
        Map<String, String> highlights = new HashMap<>();
        
        if (user.getUsername() != null && user.getUsername().toLowerCase().contains(query)) {
            highlights.put("username", highlightText(user.getUsername(), query));
        }
        if (user.getBio() != null && user.getBio().toLowerCase().contains(query)) {
            highlights.put("bio", highlightText(user.getBio(), query));
        }
        
        return highlights;
    }

    private Map<String, String> highlightCommentContent(Comment comment, String query) {
        Map<String, String> highlights = new HashMap<>();
        
        if (comment.getContent() != null && comment.getContent().toLowerCase().contains(query)) {
            highlights.put("content", highlightText(comment.getContent(), query));
        }
        
        return highlights;
    }

    private String highlightText(String text, String query) {
        return text.replaceAll("(?i)(" + query + ")", "**$1**");
    }

    @Data
    public static class SearchResult {
        private String query;
        private long timestamp;
        private List<VideoSearchResult> videos;
        private List<UserSearchResult> users;
        private List<CommentSearchResult> comments;
        private int totalResults;
        private long searchTime;
    }

    @Data
    public static class VideoSearchResult {
        private Video video;
        private double relevanceScore;
        private List<String> matchReasons;
        private Map<String, String> highlights;
    }

    @Data
    public static class UserSearchResult {
        private User user;
        private double relevanceScore;
        private List<String> matchReasons;
        private Map<String, String> highlights;
    }

    @Data
    public static class CommentSearchResult {
        private Comment comment;
        private double relevanceScore;
        private List<String> matchReasons;
        private Map<String, String> highlights;
    }
}

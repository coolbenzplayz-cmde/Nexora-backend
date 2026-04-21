package org.example.nexora.recommendation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.video.Video;
import org.example.nexora.user.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced AI Recommendation Engine that learns user behavior and predicts:
 * - What to watch
 * - Who to follow  
 * - What to buy
 * - What to engage with
 */
@Slf4j
public class AIRecommendationEngine {

    private static final double SIMILARITY_THRESHOLD = 0.3;
    private static final int MAX_RECOMMENDATIONS = 50;
    private static final int MIN_INTERACTIONS_FOR_LEARNING = 5;

    /**
     * Generate personalized video recommendations for a user
     */
    public List<VideoRecommendation> generateVideoRecommendations(User user, RecommendationContext context) {
        log.info("Generating AI video recommendations for user {}", user.getId());

        List<Video> candidateVideos = context.getAvailableVideos();
        
        // Calculate recommendation scores using multiple AI models
        List<VideoRecommendation> recommendations = new ArrayList<>();
        
        for (Video video : candidateVideos) {
            if (shouldRecommendVideo(video, user, context)) {
                double score = calculateVideoRecommendationScore(video, user, context);
                recommendations.add(new VideoRecommendation(video, score, getRecommendationReason(video, user, context)));
            }
        }

        // Sort by score and apply diversity filtering
        List<VideoRecommendation> sortedRecommendations = recommendations.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(MAX_RECOMMENDATIONS * 2) // Get more candidates for diversity filtering
                .collect(Collectors.toList());

        return applyDiversityFiltering(sortedRecommendations, user, context);
    }

    /**
     * Generate user recommendations (who to follow)
     */
    public List<UserRecommendation> generateUserRecommendations(User user, RecommendationContext context) {
        log.info("Generating AI user recommendations for user {}", user.getId());

        List<User> candidateUsers = context.getAvailableUsers();
        List<UserRecommendation> recommendations = new ArrayList<>();

        for (User candidate : candidateUsers) {
            if (shouldRecommendUser(candidate, user, context)) {
                double score = calculateUserRecommendationScore(candidate, user, context);
                recommendations.add(new UserRecommendation(candidate, score, getUserRecommendationReason(candidate, user, context)));
            }
        }

        return recommendations.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * Generate content category recommendations
     */
    public List<CategoryRecommendation> generateCategoryRecommendations(User user, RecommendationContext context) {
        Map<String, Double> categoryScores = calculateCategoryScores(user, context);
        
        return categoryScores.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.1)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(entry -> new CategoryRecommendation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Calculate video recommendation score using multiple AI models
     */
    private double calculateVideoRecommendationScore(Video video, User user, RecommendationContext context) {
        // Collaborative filtering score
        double collaborativeScore = calculateCollaborativeFilteringScore(video, user, context);
        
        // Content-based filtering score
        double contentBasedScore = calculateContentBasedScore(video, user, context);
        
        // User behavior pattern score
        double behaviorScore = calculateBehaviorPatternScore(video, user, context);
        
        // Social graph score
        double socialScore = calculateSocialGraphScore(video, user, context);
        
        // Time-based relevance score
        double timeScore = calculateTimeRelevanceScore(video, user, context);
        
        // Engagement prediction score
        double engagementScore = predictEngagementScore(video, user, context);

        // Weighted combination of all scores
        double totalScore = 
            (collaborativeScore * 0.30) +
            (contentBasedScore * 0.25) +
            (behaviorScore * 0.20) +
            (socialScore * 0.15) +
            (timeScore * 0.05) +
            (engagementScore * 0.05);

        // Apply personalization multiplier based on user preferences
        double personalizationMultiplier = getPersonalizationMultiplier(user, context);
        
        return Math.min(totalScore * personalizationMultiplier, 1.0);
    }

    /**
     * Collaborative filtering: Find users similar to the target user
     */
    private double calculateCollaborativeFilteringScore(Video video, User user, RecommendationContext context) {
        UserBehaviorProfile userProfile = context.getUserBehaviorProfiles().get(user.getId());
        if (userProfile == null || userProfile.getInteractionHistory().size() < MIN_INTERACTIONS_FOR_LEARNING) {
            return 0.1; // Default score for new users
        }

        // Find similar users
        List<UserBehaviorProfile> similarUsers = findSimilarUsers(userProfile, context.getUserBehaviorProfiles());
        
        if (similarUsers.isEmpty()) {
            return 0.1;
        }

        // Calculate score based on similar users' interactions with this video
        double score = 0.0;
        int similarUserCount = 0;

        for (UserBehaviorProfile similarUser : similarUsers) {
            if (similarUser.hasInteractedWithVideo(video.getId())) {
                score += similarUser.getVideoInteractionScore(video.getId());
                similarUserCount++;
            }
        }

        return similarUserCount > 0 ? score / similarUserCount : 0.1;
    }

    /**
     * Content-based filtering: Match video attributes to user preferences
     */
    private double calculateContentBasedScore(Video video, User user, RecommendationContext context) {
        UserPreferences preferences = context.getUserPreferences().get(user.getId());
        if (preferences == null) {
            return 0.5; // Neutral score
        }

        double score = 0.0;
        int factors = 0;

        // Duration preference
        if (video.getDurationSeconds() != null) {
            double durationScore = calculateDurationPreferenceScore(video.getDurationSeconds(), preferences);
            score += durationScore;
            factors++;
        }

        // Category preference
        if (video.getDescription() != null && preferences.getPreferredCategories() != null) {
            double categoryScore = calculateCategoryPreferenceScore(video.getDescription(), preferences.getPreferredCategories());
            score += categoryScore;
            factors++;
        }

        // Quality preference
        double qualityScore = calculateVideoQualityScore(video);
        score += qualityScore;
        factors++;

        // Creator preference
        double creatorScore = calculateCreatorPreferenceScore(video.getUserId(), preferences.getPreferredCreators());
        score += creatorScore;
        factors++;

        return factors > 0 ? score / factors : 0.5;
    }

    /**
     * Behavior pattern analysis: Learn from user's historical behavior patterns
     */
    private double calculateBehaviorPatternScore(Video video, User user, RecommendationContext context) {
        UserBehaviorProfile profile = context.getUserBehaviorProfiles().get(user.getId());
        if (profile == null) {
            return 0.5;
        }

        // Time of day preference
        double timePreferenceScore = calculateTimeOfDayPreferenceScore(video, profile);
        
        // Session pattern preference
        double sessionPatternScore = calculateSessionPatternScore(video, profile);
        
        // Content sequence preference
        double sequenceScore = calculateContentSequenceScore(video, profile);

        return (timePreferenceScore + sessionPatternScore + sequenceScore) / 3.0;
    }

    /**
     * Social graph scoring: Boost content from user's social network
     */
    private double calculateSocialGraphScore(Video video, User user, RecommendationContext context) {
        Set<Long> followingIds = context.getFollowingIds().getOrDefault(user.getId(), Collections.emptySet());
        Set<Long> friendsIds = context.getFriendIds().getOrDefault(user.getId(), Collections.emptySet());

        double score = 0.0;

        // Direct follow boost
        if (followingIds.contains(video.getUserId())) {
            score += 0.8;
        }

        // Friends of friends boost (second-degree connection)
        for (Long friendId : friendsIds) {
            Set<Long> friendsOfFriend = context.getFollowingIds().getOrDefault(friendId, Collections.emptySet());
            if (friendsOfFriend.contains(video.getUserId())) {
                score += 0.3;
                break;
            }
        }

        // Popular among follows boost
        double popularityAmongFollows = calculatePopularityAmongFollows(video, followingIds, context);
        score += popularityAmongFollows * 0.4;

        return Math.min(score, 1.0);
    }

    /**
     * Time relevance scoring: Fresh content vs evergreen content
     */
    private double calculateTimeRelevanceScore(Video video, User user, RecommendationContext context) {
        LocalDateTime now = LocalDateTime.now();
        long hoursOld = java.time.Duration.between(video.getCreatedAt(), now).toHours();

        UserBehaviorProfile profile = context.getUserBehaviorProfiles().get(user.getId());
        boolean prefersFreshContent = profile != null && profile.prefersFreshContent();

        if (prefersFreshContent) {
            // Exponential decay for fresh content preference
            if (hoursOld < 1) return 1.0;
            if (hoursOld < 6) return 0.9;
            if (hoursOld < 24) return 0.7;
            if (hoursOld < 72) return 0.5;
            if (hoursOld < 168) return 0.3;
            return 0.1;
        } else {
            // Balanced approach for evergreen content
            if (hoursOld < 24) return 0.8;
            if (hoursOld < 168) return 1.0;
            if (hoursOld < 720) return 0.9;
            return 0.7;
        }
    }

    /**
     * Predict engagement likelihood
     */
    private double predictEngagementScore(Video video, User user, RecommendationContext context) {
        UserBehaviorProfile profile = context.getUserBehaviorProfiles().get(user.getId());
        if (profile == null) {
            return 0.5;
        }

        // Historical engagement patterns
        double historicalEngagement = profile.getAverageEngagementScore();
        
        // Video-specific factors
        double videoFactors = (video.getViews() > 1000 ? 0.2 : 0.1) +
                             (video.getLikes() > 100 ? 0.2 : 0.1) +
                             (video.getComments() > 10 ? 0.2 : 0.1) +
                             (video.getEngagementScore() > 5.0 ? 0.3 : 0.1);

        return (historicalEngagement + videoFactors) / 2.0;
    }

    /**
     * Apply diversity filtering to avoid filter bubbles
     */
    private List<VideoRecommendation> applyDiversityFiltering(List<VideoRecommendation> recommendations, User user, RecommendationContext context) {
        List<VideoRecommendation> diverseRecommendations = new ArrayList<>();
        Set<Long> usedCreators = new HashSet<>();
        Set<String> usedCategories = new HashSet<>();
        
        for (VideoRecommendation rec : recommendations) {
            Video video = rec.getVideo();
            
            // Diversity rules
            boolean creatorDiversity = !usedCreators.contains(video.getUserId()) || usedCreators.size() < 3;
            boolean categoryDiversity = !usedCategories.contains(extractCategory(video)) || usedCategories.size() < 5;
            
            if (creatorDiversity && categoryDiversity) {
                diverseRecommendations.add(rec);
                usedCreators.add(video.getUserId());
                usedCategories.add(extractCategory(video));
                
                if (diverseRecommendations.size() >= MAX_RECOMMENDATIONS) {
                    break;
                }
            }
        }
        
        return diverseRecommendations;
    }

    // Helper methods for recommendation logic
    private boolean shouldRecommendVideo(Video video, User user, RecommendationContext context) {
        // Don't recommend user's own videos
        if (video.getUserId().equals(user.getId())) {
            return false;
        }
        
        // Don't recommend videos already watched
        if (context.getWatchedVideoIds().contains(video.getId())) {
            return false;
        }
        
        // Don't recommend flagged content
        if (video.getContentModerationFlagged()) {
            return false;
        }
        
        // Only recommend published videos
        if (video.getStatus() != Video.VideoStatus.PUBLISHED) {
            return false;
        }
        
        return true;
    }

    private boolean shouldRecommendUser(User candidate, User user, RecommendationContext context) {
        // Don't recommend self
        if (candidate.getId().equals(user.getId())) {
            return false;
        }
        
        // Don't recommend already followed users
        if (context.getFollowingIds().getOrDefault(user.getId(), Collections.emptySet()).contains(candidate.getId())) {
            return false;
        }
        
        // Only recommend active users
        if (candidate.getStatus() != User.UserStatus.ACTIVE) {
            return false;
        }
        
        return true;
    }

    private double calculateUserRecommendationScore(User candidate, User user, RecommendationContext context) {
        double score = 0.0;
        
        // Creator score
        if (candidate.getRole() == org.example.nexora.user.UserRole.CREATOR) {
            score += 0.3;
            if (candidate.getIsCreatorVerified()) {
                score += 0.2;
            }
        }
        
        // Similarity score
        double similarityScore = calculateUserSimilarity(user, candidate, context);
        score += similarityScore * 0.4;
        
        // Popularity score
        double popularityScore = Math.log10(candidate.getFollowersCount() + 1) / 10.0;
        score += Math.min(popularityScore, 0.3);
        
        return Math.min(score, 1.0);
    }

    private List<UserBehaviorProfile> findSimilarUsers(UserBehaviorProfile target, Map<Long, UserBehaviorProfile> allProfiles) {
        return allProfiles.values().stream()
                .filter(profile -> !profile.getUserId().equals(target.getUserId()))
                .filter(profile -> profile.getInteractionHistory().size() >= MIN_INTERACTIONS_FOR_LEARNING)
                .map(profile -> new AbstractMap.SimpleEntry<>(profile, calculateUserSimilarity(target, profile)))
                .filter(entry -> entry.getValue() >= SIMILARITY_THRESHOLD)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(20)
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateUserSimilarity(UserBehaviorProfile user1, UserBehaviorProfile user2) {
        // Simplified cosine similarity calculation
        Set<Long> commonVideos = new HashSet<>(user1.getInteractionHistory().keySet());
        commonVideos.retainAll(user2.getInteractionHistory().keySet());
        
        if (commonVideos.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (Long videoId : commonVideos) {
            double score1 = user1.getVideoInteractionScore(videoId);
            double score2 = user2.getVideoInteractionScore(videoId);
            
            dotProduct += score1 * score2;
            norm1 += score1 * score1;
            norm2 += score2 * score2;
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // Additional helper methods would be implemented here...
    private String getRecommendationReason(Video video, User user, RecommendationContext context) {
        return "AI-powered recommendation based on your viewing patterns";
    }

    private String getUserRecommendationReason(User candidate, User user, RecommendationContext context) {
        return "Recommended based on similar users you follow";
    }

    private Map<String, Double> calculateCategoryScores(User user, RecommendationContext context) {
        // Implementation would analyze user's category preferences
        return new HashMap<>();
    }

    private double getPersonalizationMultiplier(User user, RecommendationContext context) {
        // Implementation would return personalization factor based on user data availability
        return 1.0;
    }

    private String extractCategory(Video video) {
        // Simple category extraction from video description
        if (video.getDescription() != null) {
            String desc = video.getDescription().toLowerCase();
            if (desc.contains("music") || desc.contains("song")) return "music";
            if (desc.contains("gaming") || desc.contains("game")) return "gaming";
            if (desc.contains("comedy") || desc.contains("funny")) return "comedy";
            if (desc.contains("education") || desc.contains("learn")) return "education";
            if (desc.contains("sports") || desc.contains("fitness")) return "sports";
        }
        return "general";
    }

    // Additional scoring methods would be implemented with full ML algorithms...
    private double calculateDurationPreferenceScore(Integer duration, UserPreferences preferences) { return 0.5; }
    private double calculateCategoryPreferenceScore(String description, Set<String> categories) { return 0.5; }
    private double calculateVideoQualityScore(Video video) { return 0.5; }
    private double calculateCreatorPreferenceScore(Long creatorId, Set<Long> preferredCreators) { return 0.5; }
    private double calculateTimeOfDayPreferenceScore(Video video, UserBehaviorProfile profile) { return 0.5; }
    private double calculateSessionPatternScore(Video video, UserBehaviorProfile profile) { return 0.5; }
    private double calculateContentSequenceScore(Video video, UserBehaviorProfile profile) { return 0.5; }
    private double calculatePopularityAmongFollows(Video video, Set<Long> followingIds, RecommendationContext context) { return 0.5; }

    @Data
    public static class VideoRecommendation {
        private final Video video;
        private final double score;
        private final String reason;
    }

    @Data
    public static class UserRecommendation {
        private final User user;
        private final double score;
        private final String reason;
    }

    @Data
    public static class CategoryRecommendation {
        private final String category;
        private final double score;
    }
}

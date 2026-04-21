package org.example.nexora.feed;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.video.Video;
import org.example.nexora.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * TikTok-style feed algorithm that ranks videos based on multiple factors:
 * - User follows (high priority)
 * - Engagement metrics (likes, comments, shares, views)
 * - Recency (newer content gets boost)
 * - User interaction history
 * - Content quality signals
 */
@Slf4j
public class FeedAlgorithm {

    private static final double FOLLOW_WEIGHT = 0.35;      // 35% weight for following creators
    private static final double ENGAGEMENT_WEIGHT = 0.25;  // 25% weight for engagement metrics
    private static final double RECENCY_WEIGHT = 0.20;     // 20% weight for recency
    private static final double QUALITY_WEIGHT = 0.15;     // 15% weight for content quality
    private static final double DIVERSITY_WEIGHT = 0.05;   // 5% weight for content diversity

    /**
     * Calculate feed score for a video for a specific user
     */
    public double calculateFeedScore(Video video, User user, FeedContext context) {
        double followScore = calculateFollowScore(video, user, context.getFollowingIds());
        double engagementScore = calculateEngagementScore(video, context);
        double recencyScore = calculateRecencyScore(video, LocalDateTime.now());
        double qualityScore = calculateQualityScore(video, context);
        double diversityScore = calculateDiversityScore(video, context);

        double totalScore = 
            (followScore * FOLLOW_WEIGHT) +
            (engagementScore * ENGAGEMENT_WEIGHT) +
            (recencyScore * RECENCY_WEIGHT) +
            (qualityScore * QUALITY_WEIGHT) +
            (diversityScore * DIVERSITY_WEIGHT);

        log.debug("Feed score for video {}: {} (follow: {}, engagement: {}, recency: {}, quality: {}, diversity: {})",
            video.getId(), totalScore, followScore, engagementScore, recencyScore, qualityScore, diversityScore);

        return totalScore;
    }

    /**
     * Follow score: High if user follows the creator
     */
    private double calculateFollowScore(Video video, User user, Set<Long> followingIds) {
        if (followingIds.contains(video.getUserId())) {
            // Base follow score with boost for verified creators
            double baseScore = 0.8;
            if (video.getUser() != null && video.getUser().getIsCreatorVerified()) {
                baseScore += 0.2; // Boost for verified creators
            }
            return Math.min(baseScore, 1.0);
        }
        
        // Small boost for users with similar interests (based on interaction patterns)
        return 0.1;
    }

    /**
     * Engagement score: Based on likes, comments, shares, and views
     */
    private double calculateEngagementScore(Video video, FeedContext context) {
        long totalInteractions = video.getLikes() + video.getComments() + video.getShares();
        
        // Normalize by video age to prevent old videos from dominating
        double ageInHours = (double) (System.currentTimeMillis() - video.getCreatedAt().toEpochSecond() * 1000) / (1000 * 60 * 60);
        double normalizedEngagement = totalInteractions / Math.max(ageInHours, 1);
        
        // Logarithmic scaling to prevent extreme values
        double engagementScore = Math.log10(normalizedEngagement + 1) / Math.log10(1000);
        
        return Math.min(engagementScore, 1.0);
    }

    /**
     * Recency score: Newer content gets higher scores
     */
    private double calculateRecencyScore(Video video, LocalDateTime now) {
        long hoursOld = java.time.Duration.between(video.getCreatedAt(), now).toHours();
        
        // Exponential decay: very recent content gets high scores
        if (hoursOld < 1) return 1.0;
        if (hoursOld < 6) return 0.9;
        if (hoursOld < 24) return 0.8;
        if (hoursOld < 72) return 0.6;
        if (hoursOld < 168) return 0.4; // 1 week
        if (hoursOld < 720) return 0.2; // 1 month
        
        return 0.1; // Very old content
    }

    /**
     * Quality score: Based on video metadata and creator reputation
     */
    private double calculateQualityScore(Video video, FeedContext context) {
        double score = 0.5; // Base score
        
        // Boost for videos with thumbnails
        if (video.getThumbnailUrl() != null && !video.getThumbnailUrl().isEmpty()) {
            score += 0.1;
        }
        
        // Boost for videos with descriptions
        if (video.getDescription() != null && video.getDescription().length() > 10) {
            score += 0.1;
        }
        
        // Boost for verified creators
        if (video.getUser() != null && video.getUser().getIsCreatorVerified()) {
            score += 0.2;
        }
        
        // Boost for appropriate video duration (15-60 seconds is optimal for short-form)
        if (video.getDurationSeconds() != null) {
            int duration = video.getDurationSeconds();
            if (duration >= 15 && duration <= 60) {
                score += 0.1;
            } else if (duration > 300) { // Penalty for very long videos
                score -= 0.1;
            }
        }
        
        // Penalty for flagged content
        if (video.getContentModerationFlagged()) {
            score -= 0.5;
        }
        
        return Math.max(0.0, Math.min(score, 1.0));
    }

    /**
     * Diversity score: Ensures content variety in the feed
     */
    private double calculateDiversityScore(Video video, FeedContext context) {
        // Boost content from creators the user hasn't seen recently
        if (!context.getRecentCreatorIds().contains(video.getUserId())) {
            return 0.8;
        }
        
        // Lower score if user has already seen many videos from this creator
        long recentVideosFromCreator = context.getRecentVideoIds().stream()
            .filter(videoId -> {
                // This would need to be implemented with actual video lookup
                return true; // Placeholder
            })
            .count();
            
        if (recentVideosFromCreator > 3) {
            return 0.2;
        }
        
        return 0.5;
    }

    /**
     * Calculate "For You" page feed
     */
    public List<VideoScore> calculateForYouFeed(List<Video> videos, User user, FeedContext context) {
        return videos.stream()
            .map(video -> {
                double score = calculateFeedScore(video, user, context);
                return new VideoScore(video, score);
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(50) // Limit to top 50 videos
            .toList();
    }

    /**
     * Calculate "Following" feed (only from followed creators)
     */
    public List<VideoScore> calculateFollowingFeed(List<Video> videos, User user, FeedContext context) {
        return videos.stream()
            .filter(video -> context.getFollowingIds().contains(video.getUserId()))
            .map(video -> {
                double score = calculateFeedScore(video, user, context);
                // Boost following feed scores
                score *= 1.2;
                return new VideoScore(video, score);
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(100) // More videos for following feed
            .toList();
    }

    /**
     * Calculate trending videos (based on recent engagement spikes)
     */
    public List<VideoScore> calculateTrendingFeed(List<Video> videos, FeedContext context) {
        LocalDateTime trendingWindow = LocalDateTime.now().minusHours(24);
        
        return videos.stream()
            .filter(video -> video.getCreatedAt().isAfter(trendingWindow))
            .map(video -> {
                // Trending score focuses on recent engagement velocity
                double engagementVelocity = calculateEngagementVelocity(video, context);
                double trendingScore = engagementVelocity * 0.7 + calculateRecencyScore(video, LocalDateTime.now()) * 0.3;
                return new VideoScore(video, trendingScore);
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(20)
            .toList();
    }

    /**
     * Calculate engagement velocity (rate of engagement over time)
     */
    private double calculateEngagementVelocity(Video video, FeedContext context) {
        // This would ideally use time-series engagement data
        // For now, use total engagement as a proxy
        long totalEngagement = video.getLikes() + video.getComments() + video.getShares();
        double ageInHours = (double) (System.currentTimeMillis() - video.getCreatedAt().toEpochSecond() * 1000) / (1000 * 60 * 60);
        
        return totalEngagement / Math.max(ageInHours, 1);
    }

    @Data
    public static class VideoScore {
        private final Video video;
        private final double score;
        
        public VideoScore(Video video, double score) {
            this.video = video;
            this.score = score;
        }
    }

    @Data
    public static class FeedContext {
        private final Set<Long> followingIds;
        private final Set<Long> recentVideoIds;
        private final Set<Long> recentCreatorIds;
        private final UserPreferences preferences;
        
        public FeedContext(Set<Long> followingIds, Set<Long> recentVideoIds, 
                          Set<Long> recentCreatorIds, UserPreferences preferences) {
            this.followingIds = followingIds;
            this.recentVideoIds = recentVideoIds;
            this.recentCreatorIds = recentCreatorIds;
            this.preferences = preferences;
        }
    }

    @Data
    public static class UserPreferences {
        private final Set<String> interestedCategories;
        private final Set<Long> preferredCreators;
        private final double contentLengthPreference; // 0.0 = short, 1.0 = long
        private final double diversityPreference; // 0.0 = similar content, 1.0 = diverse content
    }
}

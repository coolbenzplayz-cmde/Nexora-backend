package org.example.nexora.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.social.FollowService;
import org.example.nexora.video.Video;
import org.example.nexora.video.VideoRepository;
import org.example.nexora.video.VideoStatus;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FollowService followService;
    private final FeedAlgorithm feedAlgorithm;

    /**
     * Get personalized "For You" feed for a user
     */
    public List<FeedAlgorithm.VideoScore> getForYouFeed(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's following list
        Set<Long> followingIds = followService.getFollowingIds(userId);
        
        // Get recent video interactions (simplified - would use actual interaction history)
        Set<Long> recentVideoIds = getRecentVideoIds(userId, 50);
        Set<Long> recentCreatorIds = getRecentCreatorIds(userId, 20);
        
        // Create feed context
        FeedAlgorithm.FeedContext context = new FeedAlgorithm.FeedContext(
            followingIds, 
            recentVideoIds, 
            recentCreatorIds,
            createUserPreferences(user)
        );

        // Get candidate videos (published videos, not from user themselves)
        List<Video> candidateVideos = getCandidateVideos(userId, 500);
        
        // Calculate feed scores
        List<FeedAlgorithm.VideoScore> feedScores = feedAlgorithm.calculateForYouFeed(candidateVideos, user, context);
        
        // Limit results
        return feedScores.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get "Following" feed (only from followed creators)
     */
    public List<FeedAlgorithm.VideoScore> getFollowingFeed(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Long> followingIds = followService.getFollowingIds(userId);
        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> recentVideoIds = getRecentVideoIds(userId, 50);
        Set<Long> recentCreatorIds = getRecentCreatorIds(userId, 20);
        
        FeedAlgorithm.FeedContext context = new FeedAlgorithm.FeedContext(
            followingIds, 
            recentVideoIds, 
            recentCreatorIds,
            createUserPreferences(user)
        );

        // Get videos from followed creators
        List<Video> candidateVideos = videoRepository.findByUserIdInAndStatusOrderByCreatedAtDesc(
            new ArrayList<>(followingIds), VideoStatus.PUBLISHED);
        
        List<FeedAlgorithm.VideoScore> feedScores = feedAlgorithm.calculateFollowingFeed(candidateVideos, user, context);
        
        return feedScores.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get trending videos
     */
    public List<FeedAlgorithm.VideoScore> getTrendingFeed(int limit) {
        // Get recent videos from last 24 hours
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Video> recentVideos = videoRepository.findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(
            VideoStatus.PUBLISHED, since);
        
        FeedAlgorithm.FeedContext context = new FeedAlgorithm.FeedContext(
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet(),
            new FeedAlgorithm.UserPreferences(
                Collections.emptySet(),
                Collections.emptySet(),
                0.5,
                0.5
            )
        );
        
        List<FeedAlgorithm.VideoScore> trendingScores = feedAlgorithm.calculateTrendingFeed(recentVideos, context);
        
        return trendingScores.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get recommended videos based on a specific video
     */
    public List<FeedAlgorithm.VideoScore> getRecommendedVideos(Long videoId, Long userId, int limit) {
        Video sourceVideo = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get videos from the same creator or similar content
        List<Video> candidateVideos = new ArrayList<>();
        
        // Add videos from the same creator
        candidateVideos.addAll(videoRepository.findByUserIdAndStatusAndIdNotOrderByCreatedAtDesc(
            sourceVideo.getUserId(), VideoStatus.PUBLISHED, videoId));
        
        // Add videos from followed creators (if user follows them)
        Set<Long> followingIds = followService.getFollowingIds(userId);
        if (!followingIds.isEmpty()) {
            candidateVideos.addAll(videoRepository.findByUserIdInAndStatusOrderByCreatedAtDesc(
                new ArrayList<>(followingIds), VideoStatus.PUBLISHED));
        }
        
        // Remove duplicates and limit
        candidateVideos = candidateVideos.stream()
            .distinct()
            .limit(100)
            .collect(Collectors.toList());

        Set<Long> recentVideoIds = getRecentVideoIds(userId, 50);
        Set<Long> recentCreatorIds = getRecentCreatorIds(userId, 20);
        
        FeedAlgorithm.FeedContext context = new FeedAlgorithm.FeedContext(
            followingIds, 
            recentVideoIds, 
            recentCreatorIds,
            createUserPreferences(user)
        );

        return candidateVideos.stream()
            .map(video -> {
                double score = feedAlgorithm.calculateFeedScore(video, user, context);
                // Boost similar content
                if (video.getUserId().equals(sourceVideo.getUserId())) {
                    score *= 1.3;
                }
                return new FeedAlgorithm.VideoScore(video, score);
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get videos from a specific category or topic
     */
    public List<FeedAlgorithm.VideoScore> getCategoryFeed(String category, Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // This would ideally use video categories/tags
        // For now, get all published videos and filter by description contains category
        List<Video> categoryVideos = videoRepository.findByStatusAndDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
            VideoStatus.PUBLISHED, category);

        Set<Long> followingIds = followService.getFollowingIds(userId);
        Set<Long> recentVideoIds = getRecentVideoIds(userId, 50);
        Set<Long> recentCreatorIds = getRecentCreatorIds(userId, 20);
        
        FeedAlgorithm.FeedContext context = new FeedAlgorithm.FeedContext(
            followingIds, 
            recentVideoIds, 
            recentCreatorIds,
            createUserPreferences(user)
        );

        return categoryVideos.stream()
            .map(video -> {
                double score = feedAlgorithm.calculateFeedScore(video, user, context);
                return new FeedAlgorithm.VideoScore(video, score);
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Refresh feed with new content
     */
    public List<FeedAlgorithm.VideoScore> refreshFeed(Long userId, String feedType, int limit) {
        switch (feedType.toLowerCase()) {
            case "foryou":
                return getForYouFeed(userId, limit);
            case "following":
                return getFollowingFeed(userId, limit);
            case "trending":
                return getTrendingFeed(limit);
            default:
                return getForYouFeed(userId, limit);
        }
    }

    /**
     * Get candidate videos for feed generation
     */
    private List<Video> getCandidateVideos(Long userId, int limit) {
        // Get published videos, excluding user's own videos
        return videoRepository.findByStatusAndUserIdNotOrderByCreatedAtDesc(
            VideoStatus.PUBLISHED, userId)
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get recent video IDs the user has interacted with
     */
    private Set<Long> getRecentVideoIds(Long userId, int limit) {
        // This would use actual interaction history (views, likes, comments)
        // For now, return empty set
        return Collections.emptySet();
    }

    /**
     * Get recent creator IDs the user has interacted with
     */
    private Set<Long> getRecentCreatorIds(Long userId, int limit) {
        // This would use actual interaction history
        // For now, return following IDs as a proxy
        return followService.getFollowingIds(userId).stream()
            .limit(limit)
            .collect(Collectors.toSet());
    }

    /**
     * Create user preferences based on user data
     */
    private FeedAlgorithm.UserPreferences createUserPreferences(User user) {
        // This would use actual user preference data
        // For now, use default preferences
        return new FeedAlgorithm.UserPreferences(
            Collections.emptySet(), // interestedCategories
            Collections.emptySet(), // preferredCreators
            0.5, // contentLengthPreference (balanced)
            0.7  // diversityPreference (prefer diverse content)
        );
    }

    /**
     * Record video interaction for future feed optimization
     */
    public void recordVideoInteraction(Long userId, Long videoId, String interactionType) {
        // This would store interaction data for future feed optimization
        // Types: "view", "like", "comment", "share", "complete", "skip"
        log.info("Recorded interaction: user {} on video {} - {}", userId, videoId, interactionType);
    }

    /**
     * Get feed statistics for analytics
     */
    public Map<String, Object> getFeedStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get basic stats
        stats.put("followingCount", followService.getFollowingCount(userId));
        stats.put("totalVideos", videoRepository.countByStatus(VideoStatus.PUBLISHED));
        stats.put("trendingCount", getTrendingFeed(20).size());
        
        return stats;
    }
}

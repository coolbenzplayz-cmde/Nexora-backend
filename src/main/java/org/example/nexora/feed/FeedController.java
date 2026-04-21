package org.example.nexora.feed;

import lombok.RequiredArgsConstructor;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/foryou")
    public ResponseEntity<ApiResponse<List<FeedAlgorithm.VideoScore>>> getForYouFeed(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<FeedAlgorithm.VideoScore> feed = feedService.getForYouFeed(currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(feed, "For You feed generated successfully"));
    }

    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<FeedAlgorithm.VideoScore>>> getFollowingFeed(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<FeedAlgorithm.VideoScore> feed = feedService.getFollowingFeed(currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(feed, "Following feed generated successfully"));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<FeedAlgorithm.VideoScore>>> getTrendingFeed(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<FeedAlgorithm.VideoScore> feed = feedService.getTrendingFeed(limit);
        return ResponseEntity.ok(ApiResponse.success(feed, "Trending feed generated successfully"));
    }

    @GetMapping("/recommended/{videoId}")
    public ResponseEntity<ApiResponse<List<FeedAlgorithm.VideoScore>>> getRecommendedVideos(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<FeedAlgorithm.VideoScore> recommendations = feedService.getRecommendedVideos(
            videoId, currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations, "Recommended videos generated successfully"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<FeedAlgorithm.VideoScore>>> getCategoryFeed(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String category,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<FeedAlgorithm.VideoScore> feed = feedService.getCategoryFeed(category, currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(feed, "Category feed generated successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<List<FeedAlgorithm.VideoScore>>> refreshFeed(
            @AuthenticationPrincipal User currentUser,
            @RequestBody RefreshFeedRequest request) {
        
        List<FeedAlgorithm.VideoScore> feed = feedService.refreshFeed(
            currentUser.getId(), request.getFeedType(), request.getLimit());
        return ResponseEntity.ok(ApiResponse.success(feed, "Feed refreshed successfully"));
    }

    @PostMapping("/interact/{videoId}")
    public ResponseEntity<ApiResponse<Void>> recordVideoInteraction(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long videoId,
            @RequestBody VideoInteractionRequest request) {
        
        feedService.recordVideoInteraction(currentUser.getId(), videoId, request.getInteractionType());
        return ResponseEntity.ok(ApiResponse.success(null, "Interaction recorded successfully"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeedStatistics(
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> stats = feedService.getFeedStatistics(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(stats, "Feed statistics retrieved successfully"));
    }

    @GetMapping("/debug/scores/{videoId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugFeedScores(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long videoId) {
        
        // This would provide detailed scoring breakdown for debugging
        Map<String, Object> debugInfo = Map.of(
            "videoId", videoId,
            "userId", currentUser.getId(),
            "message", "Debug scoring not yet implemented"
        );
        
        return ResponseEntity.ok(ApiResponse.success(debugInfo, "Debug information retrieved"));
    }
}

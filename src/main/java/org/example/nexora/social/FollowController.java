package org.example.nexora.social;

import lombok.RequiredArgsConstructor;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.example.nexora.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow/{userId}")
    public ResponseEntity<ApiResponse<Follow>> followUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        Follow follow = followService.followUser(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(follow, "User followed successfully"));
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        followService.unfollowUser(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User unfollowed successfully"));
    }

    @PostMapping("/refollow/{userId}")
    public ResponseEntity<ApiResponse<Follow>> refollowUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        Follow follow = followService.refollowUser(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(follow, "User refollowed successfully"));
    }

    @GetMapping("/is-following/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        boolean isFollowing = followService.isFollowing(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(isFollowing));
    }

    @GetMapping("/following")
    public ResponseEntity<ApiResponse<PaginationResponse<Follow>>> getFollowing(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> following = followService.getFollowing(currentUser.getId(), pageable);
        
        PaginationResponse<Follow> response = PaginationResponse.<Follow>builder()
                .content(following.getContent())
                .page(following.getNumber())
                .size(following.getSize())
                .totalElements(following.getTotalElements())
                .totalPages(following.getTotalPages())
                .first(following.isFirst())
                .last(following.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<PaginationResponse<Follow>>> getFollowers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> followers = followService.getFollowers(currentUser.getId(), pageable);
        
        PaginationResponse<Follow> response = PaginationResponse.<Follow>builder()
                .content(followers.getContent())
                .page(followers.getNumber())
                .size(followers.getSize())
                .totalElements(followers.getTotalElements())
                .totalPages(followers.getTotalPages())
                .first(followers.isFirst())
                .last(followers.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/following-count")
    public ResponseEntity<ApiResponse<Long>> getFollowingCount(
            @AuthenticationPrincipal User currentUser) {
        
        long count = followService.getFollowingCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/followers-count")
    public ResponseEntity<ApiResponse<Long>> getFollowersCount(
            @AuthenticationPrincipal User currentUser) {
        
        long count = followService.getFollowersCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/following-ids")
    public ResponseEntity<ApiResponse<List<Long>>> getFollowingIds(
            @AuthenticationPrincipal User currentUser) {
        
        List<Long> ids = followService.getFollowingIds(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(ids));
    }

    @GetMapping("/follower-ids")
    public ResponseEntity<ApiResponse<List<Long>>> getFollowerIds(
            @AuthenticationPrincipal User currentUser) {
        
        List<Long> ids = followService.getFollowerIds(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(ids));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<PaginationResponse<Follow>>> getAllFollowActivity(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> activity = followService.getAllFollowActivity(currentUser.getId(), pageable);
        
        PaginationResponse<Follow> response = PaginationResponse.<Follow>builder()
                .content(activity.getContent())
                .page(activity.getNumber())
                .size(activity.getSize())
                .totalElements(activity.getTotalElements())
                .totalPages(activity.getTotalPages())
                .first(activity.isFirst())
                .last(activity.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/promote-to-creator/{userId}")
    public ResponseEntity<ApiResponse<Void>> promoteToCreator(@PathVariable Long userId) {
        followService.promoteToCreator(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User promoted to creator successfully"));
    }

    @PostMapping("/verify-creator/{userId}")
    public ResponseEntity<ApiResponse<Void>> verifyCreator(@PathVariable Long userId) {
        followService.verifyCreator(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Creator verified successfully"));
    }

    @GetMapping("/recommended-creators")
    public ResponseEntity<ApiResponse<List<User>>> getRecommendedCreators(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<User> creators = followService.getRecommendedCreators(currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(creators));
    }
}

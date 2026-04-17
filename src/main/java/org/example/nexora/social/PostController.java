package org.example.nexora.social;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.BusinessException;
import org.example.nexora.common.PaginationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing social posts.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management APIs")
public class PostController {

    private final PostService postService;

    /**
     * Creates a new post.
     */
    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<ApiResponse<Post>> createPost(
            @RequestBody Post post,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        post.setUserId(userId);

        Post createdPost = postService.createPost(post);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(createdPost));
    }

    /**
     * Gets a post by ID.
     */
    @GetMapping("/{postId}")
    @Operation(summary = "Get a post by ID")
    public ResponseEntity<ApiResponse<Post>> getPost(
            @Parameter(description = "Post ID") @PathVariable UUID postId) {

        Post post = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    /**
     * Gets the authenticated user's feed.
     */
    @GetMapping("/feed")
    @Operation(summary = "Get user feed")
    public ResponseEntity<ApiResponse<PaginationResponse<Post>>> getFeed(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        PaginationResponse<Post> feed = postService.getFeed(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    /**
     * Gets posts by user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get posts by user")
    public ResponseEntity<ApiResponse<PaginationResponse<Post>>> getUserPosts(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        PaginationResponse<Post> posts = postService.getPostsByUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * Updates a post.
     */
    @PutMapping("/{postId}")
    @Operation(summary = "Update a post")
    public ResponseEntity<ApiResponse<Post>> updatePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId,
            @RequestBody Post post,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        Post existingPost = postService.getPostById(postId);

        if (!existingPost.getUserId().equals(userId)) {
            throw BusinessException.forbidden("You can only update your own posts");
        }

        Post updatedPost = postService.updatePost(postId, post);
        return ResponseEntity.ok(ApiResponse.success(updatedPost));
    }

    /**
     * Deletes a post.
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        Post post = postService.getPostById(postId);

        if (!post.getUserId().equals(userId)) {
            throw BusinessException.forbidden("You can only delete your own posts");
        }

        postService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success(null, "Post deleted successfully"));
    }

    /**
     * Likes a post.
     */
    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId) {

        postService.incrementLikes(postId);
        return ResponseEntity.ok(ApiResponse.success(null, "Post liked"));
    }

    /**
     * Unlikes a post.
     */
    @DeleteMapping("/{postId}/like")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId) {

        postService.decrementLikes(postId);
        return ResponseEntity.ok(ApiResponse.success(null, "Post unliked"));
    }

    /**
     * Searches posts.
     */
    @GetMapping("/search")
    @Operation(summary = "Search posts")
    public ResponseEntity<ApiResponse<List<Post>>> searchPosts(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Max results") @RequestParam(defaultValue = "20") int limit) {

        List<Post> posts = postService.searchPosts(query, limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * Gets trending posts.
     */
    @GetMapping("/trending")
    @Operation(summary = "Get trending posts")
    public ResponseEntity<ApiResponse<List<Post>>> getTrendingPosts(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {

        List<Post> posts = postService.getTrendingPosts(limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * Gets user ID from authentication.
     */
    private UUID getUserIdFromAuth(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
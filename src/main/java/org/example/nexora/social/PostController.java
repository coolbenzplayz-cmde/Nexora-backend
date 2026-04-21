package org.example.nexora.social;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private UUID getUserIdFromAuth(Authentication authentication) {
        // Extract user ID from authentication
        return UUID.fromString(authentication.getName());
    }

    @PostMapping
    @Operation(summary = "Create a new post")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Post created successfully")
    public ResponseEntity<org.example.nexora.common.ApiResponse<Post>> createPost(
            @RequestBody Post post,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        post.setUserId(userId);

        org.example.nexora.common.ApiResponse<Post> response = postService.createPost(userId, post.getContent());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Gets a post by ID.
     */
    @GetMapping("/{postId}")
    @Operation(summary = "Get a post by ID")
    public ResponseEntity<org.example.nexora.common.ApiResponse<Post>> getPost(
            @Parameter(description = "Post ID") @PathVariable UUID postId) {

        Post post = postService.getPostById(postId);
        return ResponseEntity.ok(org.example.nexora.common.ApiResponse.success(post));
    }

    /**
     * Gets the authenticated user's feed.
     */
    @GetMapping("/feed")
    @Operation(summary = "Get user feed")
    public ResponseEntity<org.example.nexora.common.ApiResponse<PaginationResponse<Post>>> getFeed(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        org.example.nexora.common.ApiResponse<org.example.nexora.common.PaginationResponse<Post>> feed = postService.getUserFeed(userId, page, size);
        return ResponseEntity.ok(feed);
    }

    /**
     * Gets posts by user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get posts by user")
    public ResponseEntity<org.example.nexora.common.ApiResponse<PaginationResponse<Post>>> getUserPosts(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        org.example.nexora.common.ApiResponse<org.example.nexora.common.PaginationResponse<Post>> posts = postService.getPostsByUser(userId, page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * Updates a post.
     */
    @PutMapping("/{postId}")
    @Operation(summary = "Update a post")
    public ResponseEntity<org.example.nexora.common.ApiResponse<Post>> updatePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId,
            @RequestBody Post post,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        org.example.nexora.common.ApiResponse<Post> updatedPost = postService.updatePost(postId, userId, post.getContent());
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Deletes a post.
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<org.example.nexora.common.ApiResponse<String>> deletePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId,
            Authentication authentication) {

        UUID userId = getUserIdFromAuth(authentication);
        postService.deletePost(postId, userId);
        return ResponseEntity.ok(org.example.nexora.common.ApiResponse.success("Post deleted successfully"));
    }

    /**
     * Likes a post.
     */
    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<org.example.nexora.common.ApiResponse<String>> likePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId,
            Authentication authentication) {

        postService.incrementLikes(postId);
        return ResponseEntity.ok(org.example.nexora.common.ApiResponse.success("Post liked successfully"));
    }

    /**
     * Unlikes a post.
     */
    @PostMapping("/{postId}/unlike")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<org.example.nexora.common.ApiResponse<String>> unlikePost(
            @Parameter(description = "Post ID") @PathVariable UUID postId,
            Authentication authentication) {

        postService.decrementLikes(postId);
        return ResponseEntity.ok(org.example.nexora.common.ApiResponse.success("Post unliked successfully"));
    }

    /**
     * Searches posts.
     */
    @GetMapping("/search")
    @Operation(summary = "Search posts")
    public ResponseEntity<org.example.nexora.common.ApiResponse<List<Post>>> searchPosts(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "20") int limit) {

        List<Post> posts = postService.searchPosts(query, limit);
        return ResponseEntity.ok(org.example.nexora.common.ApiResponse.success(posts));
    }

    /**
     * Gets trending posts.
     */
    @GetMapping("/trending")
    @Operation(summary = "Get trending posts")
    public ResponseEntity<org.example.nexora.common.ApiResponse<List<Post>>> getTrendingPosts(
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "20") int limit) {

        List<Post> posts = postService.getTrendingPosts(limit);
        return ResponseEntity.ok(org.example.nexora.common.ApiResponse.success(posts));
    }
}

package org.example.nexora.social;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.BusinessException;
import org.example.nexora.common.PaginationResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    
    private final PostRepository postRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ApiResponse<Post> createPost(UUID userId, String content) {
        log.info("Creating post for user: {}", userId);
        
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setLikesCount(0L);
        post.setCommentsCount(0L);
        post.setSharesCount(0L);
        post.setViewsCount(0L);
        post.setIsEdited(false);
        
        Post savedPost = postRepository.save(post);
        
        // Send notification
        kafkaTemplate.send("post-events", Map.of(
            "type", "POST_CREATED",
            "postId", savedPost.getId(),
            "userId", userId
        ));
        
        log.info("Post created with ID: {}", savedPost.getId());
        return ApiResponse.success(savedPost);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userFeed", key = "#userId")
    public ApiResponse<PaginationResponse<Post>> getUserFeed(UUID userId, int page, int size) {
        log.info("Fetching feed for user: {}", userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        org.example.nexora.common.PaginationResponse<Post> response = new org.example.nexora.common.PaginationResponse<>(posts);
        
        return ApiResponse.success(response);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "postsByUser", key = "#userId")
    public ApiResponse<PaginationResponse<Post>> getPostsByUser(UUID userId, int page, int size) {
        log.info("Fetching posts by user: {}", userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        org.example.nexora.common.PaginationResponse<Post> response = new org.example.nexora.common.PaginationResponse<>(posts);
        
        return ApiResponse.success(response);
    }

    @Transactional
    public ApiResponse<Post> updatePost(UUID postId, UUID userId, String content) {
        log.info("Updating post: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found"));
        
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("Unauthorized", "FORBIDDEN");
        }
        
        post.setContent(content);
        post.setIsEdited(true);
        
        Post updatedPost = postRepository.save(post);
        
        // Send notification
        kafkaTemplate.send("post-events", Map.of(
            "type", "POST_UPDATED",
            "postId", updatedPost.getId(),
            "userId", userId
        ));
        
        return ApiResponse.success(updatedPost);
    }

    @Transactional
    @CacheEvict(value = {"userFeed", "postsByUser"}, key = "#userId")
    public ApiResponse<String> deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found"));
        
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("Unauthorized", "FORBIDDEN");
        }
        
        postRepository.delete(post);
        
        // Send notification
        kafkaTemplate.send("post-events", Map.of(
            "type", "POST_DELETED",
            "postId", postId,
            "userId", userId
        ));
        
        return ApiResponse.success("Post deleted successfully");
    }

    // Additional methods expected by controller
    public Post getPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found"));
    }

    public PaginationResponse<Post> getFeed(UUID userId, int page, int size) {
        return getUserFeed(userId, page, size).getData();
    }

    public void incrementLikes(UUID postId) {
        postRepository.incrementLikesCount(postId);
    }

    public void decrementLikes(UUID postId) {
        postRepository.decrementLikesCount(postId);
    }

    public List<Post> searchPosts(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.searchByContent(query, pageable);
    }

    public List<Post> getTrendingPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findTrendingPosts(pageable);
    }
}

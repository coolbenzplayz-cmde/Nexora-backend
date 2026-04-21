package org.example.nexora.social;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.BusinessException;
import org.example.nexora.common.PaginationResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing social posts.
 * Handles post creation, retrieval, update, and deletion.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Creates a new post.
     */
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post createPost(Post post) {
        log.info("Creating post for user: {}", post.getUserId());

        post.setLikesCount(0L);
        post.setCommentsCount(0L);
        post.setSharesCount(0L);
        post.setViewsCount(0L);
        post.setIsEdited(false);

        Post savedPost = postRepository.save(post);
        log.info("Post created with ID: {}", savedPost.getId());

        // Send event to Kafka
        sendPostCreatedEvent(savedPost);

        return savedPost;
    }

    /**
     * Gets a post by ID.
     */
    @Transactional(readOnly = true)
    public Post getPostById(UUID postId) {
        log.info("Fetching post: {}", postId);
        return postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("Post"));
    }

    /**
     * Gets posts for a user's feed.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#userId + '-' + #page + '-' + #size")
    public PaginationResponse<Post> getFeed(UUID userId, int page, int size) {
        log.info("Fetching feed for user: {}", userId);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByUserId(userId, pageRequest);

        return PaginationResponse.of(
                posts.getContent(),
                page,
                size,
                posts.getTotalElements()
        );
    }

    /**
     * Gets posts by user.
     */
    @Transactional(readOnly = true)
    public PaginationResponse<Post> getPostsByUser(UUID userId, int page, int size) {
        log.info("Fetching posts by user: {}", userId);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByUserIdAndIsActiveTrue(userId, pageRequest);

        return PaginationResponse.of(
                posts.getContent(),
                page,
                size,
                posts.getTotalElements()
        );
    }

    /**
     * Updates a post.
     */
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post updatePost(UUID postId, Post updatedPost) {
        log.info("Updating post: {}", postId);

        Post existingPost = getPostById(postId);
        existingPost.setContent(updatedPost.getContent());
        existingPost.setMediaUrls(updatedPost.getMediaUrls());
        existingPost.setMediaTypes(updatedPost.getMediaTypes());
        existingPost.setVisibility(updatedPost.getVisibility());
        existingPost.setLocation(updatedPost.getLocation());
        existingPost.setIsEdited(true);

        Post savedPost = postRepository.save(existingPost);
        log.info("Post updated: {}", postId);

        return savedPost;
    }

    /**
     * Deletes a post (soft delete).
     */
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void deletePost(UUID postId) {
        log.info("Deleting post: {}", postId);

        Post post = getPostById(postId);
        post.softDelete();
        postRepository.save(post);

        log.info("Post deleted: {}", postId);
    }

    /**
     * Increments like count for a post.
     */
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void incrementLikes(UUID postId) {
        log.info("Incrementing likes for post: {}", postId);
        postRepository.incrementLikesCount(postId);
    }

    /**
     * Decrements like count for a post.
     */
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void decrementLikes(UUID postId) {
        log.info("Decrementing likes for post: {}", postId);
        postRepository.decrementLikesCount(postId);
    }

    /**
     * Increments comment count for a post.
     */
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void incrementComments(UUID postId) {
        log.info("Incrementing comments for post: {}", postId);
        postRepository.incrementCommentsCount(postId);
    }

    /**
     * Searches posts by content.
     */
    @Transactional(readOnly = true)
    public List<Post> searchPosts(String query, int limit) {
        log.info("Searching posts with query: {}", query);
        return postRepository.searchByContent(query, PageRequest.of(0, limit));
    }

    /**
     * Gets trending posts.
     */
    @Transactional(readOnly = true)
    public List<Post> getTrendingPosts(int limit) {
        log.info("Fetching trending posts");
        return postRepository.findTrendingPosts(PageRequest.of(0, limit));
    }

    /**
     * Sends post created event to Kafka.
     */
    private void sendPostCreatedEvent(Post post) {
        try {
            String message = String.format("{\"event\":\"POST_CREATED\",\"postId\":\"%s\",\"userId\":\"%s\"}",
                    post.getId(), post.getUserId());
            kafkaTemplate.send("nexora.post.events", message);
            log.debug("Post created event sent: {}", post.getId());
        } catch (Exception e) {
            log.error("Failed to send post created event", e);
        }
    }
}
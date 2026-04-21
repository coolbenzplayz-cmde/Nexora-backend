package org.example.nexora.social;

import org.example.nexora.common.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing Post entities.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Find posts by user ID.
     */
    Page<Post> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find posts by user ID ordered by creation date.
     */
    Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find active posts by user ID.
     */
    Page<Post> findByUserIdAndIsActiveTrue(UUID userId, Pageable pageable);

    /**
     * Search posts by content.
     */
    @Query("SELECT p FROM Post p WHERE p.isActive = true AND LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
    List<Post> searchByContent(@Param("query") String query, Pageable pageable);

    /**
     * Find trending posts (most engagement in last 7 days).
     */
    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.createdAt > CURRENT_TIMESTAMP - 7 ORDER BY (p.likesCount + p.commentsCount + p.sharesCount) DESC")
    List<Post> findTrendingPosts(Pageable pageable);

    /**
     * Find public posts for feed.
     */
    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> findPublicPosts(Pageable pageable);

    /**
     * Find posts by visibility.
     */
    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.visibility = :visibility AND p.isActive = true ORDER BY p.createdAt DESC")
    Page<Post> findByVisibility(@Param("userId") UUID userId, @Param("visibility") Post.Visibility visibility, Pageable pageable);

    // ==================== Count Updates ====================

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.id = :postId")
    void incrementLikesCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = CASE WHEN p.likesCount > 0 THEN p.likesCount - 1 ELSE 0 END WHERE p.id = :postId")
    void decrementLikesCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentsCount = p.commentsCount + 1 WHERE p.id = :postId")
    void incrementCommentsCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.sharesCount = p.sharesCount + 1 WHERE p.id = :postId")
    void incrementSharesCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.viewsCount = p.viewsCount + 1 WHERE p.id = :postId")
    void incrementViewsCount(@Param("postId") UUID postId);
}

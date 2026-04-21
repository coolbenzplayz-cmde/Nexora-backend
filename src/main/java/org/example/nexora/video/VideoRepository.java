package org.example.nexora.video;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    // Basic queries
    Page<Video> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Video> findByStatusOrderByCreatedAtDesc(Video.VideoStatus status, Pageable pageable);
    List<Video> findByUserId(Long userId);
    long countByUserId(Long userId);

    // Enhanced queries for feed algorithm
    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' AND v.isPrivate = false ORDER BY v.createdAt DESC")
    Page<Video> findPublishedPublicVideosOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' AND v.userId IN :userIds ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdInAndStatusOrderByCreatedAtDesc(@Param("userIds") List<Long> userIds, Video.VideoStatus status, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' AND v.createdAt >= :since ORDER BY v.createdAt DESC")
    Page<Video> findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(@Param("status") Video.VideoStatus status, @Param("since") LocalDateTime since, Pageable pageable);

    // Search queries
    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' AND v.isPrivate = false AND (LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY v.createdAt DESC")
    Page<Video> searchPublishedVideos(@Param("keyword") String keyword, Pageable pageable);

    Page<Video> findByTitleContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(String title, Video.VideoStatus status, Pageable pageable);

    // Analytics queries
    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' ORDER BY v.views DESC")
    Page<Video> findTopVideosByViews(Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' ORDER BY v.likes DESC")
    Page<Video> findTopVideosByLikes(Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' ORDER BY v.engagementScore DESC")
    Page<Video> findTopVideosByEngagement(Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.status = 'PUBLISHED' ORDER BY v.views DESC")
    Page<Video> findUserVideosByViews(@Param("userId") Long userId, Pageable pageable);

    // Content moderation queries
    @Query("SELECT v FROM Video v WHERE v.contentModerationFlagged = true ORDER BY v.createdAt DESC")
    Page<Video> findFlaggedVideos(Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(v) FROM Video v WHERE v.status = 'PUBLISHED'")
    long countPublishedVideos();

    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId AND v.status = 'PUBLISHED'")
    long countPublishedVideosByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(v.views) FROM Video v WHERE v.userId = :userId AND v.status = 'PUBLISHED'")
    Long sumViewsByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(v.likes) FROM Video v WHERE v.userId = :userId AND v.status = 'PUBLISHED'")
    Long sumLikesByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(v.creatorEarnings) FROM Video v WHERE v.userId = :userId AND v.status = 'PUBLISHED'")
    Double sumEarningsByUserId(@Param("userId") Long userId);

    // Creator-specific queries
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.status IN :statuses ORDER BY v.createdAt DESC")
    Page<Video> findUserVideosByStatuses(@Param("userId") Long userId, @Param("statuses") List<Video.VideoStatus> statuses, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' AND v.durationSeconds BETWEEN :minDuration AND :maxDuration ORDER BY v.createdAt DESC")
    Page<Video> findVideosByDuration(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration, Pageable pageable);

    // Trending queries
    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' AND v.createdAt >= :since ORDER BY v.engagementScore DESC")
    Page<Video> findTrendingVideosSince(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.status = 'PUBLISHED' AND v.isPrivate = false AND v.userId != :userId ORDER BY v.engagementScore DESC")
    Page<Video> findRecommendedVideosForUser(@Param("userId") Long userId, Pageable pageable);
}

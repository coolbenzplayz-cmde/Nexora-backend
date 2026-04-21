package org.example.nexora.social;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.videoId = :videoId AND c.contentType = 'VIDEO' AND c.isDeleted = false AND c.parentCommentId IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByVideoId(@Param("videoId") Long videoId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.contentType = 'POST' AND c.isDeleted = false AND c.parentCommentId IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentCommentId = :parentCommentId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.videoId = :videoId AND c.contentType = 'VIDEO' AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findAllCommentsByVideoId(@Param("videoId") Long videoId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.contentType = 'POST' AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findAllCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.userId = :userId AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.videoId = :videoId AND c.contentType = 'VIDEO' AND c.isDeleted = false")
    long countCommentsByVideoId(@Param("videoId") Long videoId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.contentType = 'POST' AND c.isDeleted = false")
    long countCommentsByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.userId = :userId AND c.isDeleted = false")
    long countCommentsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentCommentId = :parentCommentId AND c.isDeleted = false")
    long countRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    @Query("SELECT c FROM Comment c WHERE c.contentModerationFlagged = true ORDER BY c.createdAt DESC")
    Page<Comment> findFlaggedComments(Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> searchCommentsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true, c.deletedAt = CURRENT_TIMESTAMP, c.content = '[deleted]' WHERE c.id = :commentId AND c.userId = :userId")
    int softDeleteComment(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Comment c SET c.contentModerationFlagged = true, c.contentModerationReason = :reason WHERE c.id = :commentId")
    int flagComment(@Param("commentId") Long commentId, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE Comment c SET c.contentModerationFlagged = false, c.contentModerationReason = null WHERE c.id = :commentId")
    int unflagComment(@Param("commentId") Long commentId);

    @Query("SELECT c FROM Comment c WHERE c.parentCommentId IN :parentCommentIds AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentIds(@Param("parentCommentIds") List<Long> parentCommentIds);

    @Query("SELECT c FROM Comment c WHERE c.videoId = :videoId AND c.contentType = 'VIDEO' AND c.isDeleted = false AND c.likesCount > 0 ORDER BY c.likesCount DESC, c.createdAt DESC")
    Page<Comment> findPopularCommentsByVideoId(@Param("videoId") Long videoId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.contentType = 'POST' AND c.isDeleted = false AND c.likesCount > 0 ORDER BY c.likesCount DESC, c.createdAt DESC")
    Page<Comment> findPopularCommentsByPostId(@Param("postId") Long postId, Pageable pageable);
}

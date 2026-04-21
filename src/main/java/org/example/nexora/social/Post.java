package org.example.nexora.social;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Social media post entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_user_id", columnList = "userId"),
        @Index(name = "idx_posts_created_at", columnList = "createdAt"),
        @Index(name = "idx_posts_likes_count", columnList = "likesCount"),
        @Index(name = "idx_posts_comments_count", columnList = "commentsCount")
})
public class Post extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type = PostType.POST;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "likes_count")
    private Long likesCount = 0L;

    @Column(name = "comments_count")
    private Long commentsCount = 0L;

    @Column(name = "shares_count")
    private Long sharesCount = 0L;

    @Column(name = "views_count")
    private Long viewsCount = 0L;

    private String location;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    @Column(name = "is_edited")
    private Boolean isEdited = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    /**
     * Post type enum
     */
    public enum PostType {
        POST,
        STORY,
        REEL,
        LIVE
    }

    // Explicit getters and setters to ensure they exist
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Long likesCount) {
        this.likesCount = likesCount;
    }

    public Long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Long getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(Long sharesCount) {
        this.sharesCount = sharesCount;
    }

    public Long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }
}

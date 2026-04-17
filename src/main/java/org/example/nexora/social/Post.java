package org.example.nexora.social;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Post entity representing user posts in the social feed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_user_id", columnList = "userId"),
        @Index(name = "idx_posts_created_at", columnList = "createdAt DESC"),
        @Index(name = "idx_posts_visibility", columnList = "visibility")
})
public class Post extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Array(columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "media_urls", columnDefinition = "TEXT[]")
    private List<String> mediaUrls = new ArrayList<>();

    @Array(columnDefinition = "VARCHAR(20)")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "media_types", columnDefinition = "VARCHAR(20)[]")
    private List<String> mediaTypes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType = PostType.POST;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility = Visibility.PUBLIC;

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

    /**
     * Visibility enum
     */
    public enum Visibility {
        PUBLIC,
        FOLLOWERS,
        PRIVATE
    }

    /**
     * Pre-persist hook
     */
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (likesCount == null) likesCount = 0L;
        if (commentsCount == null) commentsCount = 0L;
        if (sharesCount == null) sharesCount = 0L;
        if (viewsCount == null) viewsCount = 0L;
    }

    /**
     * Get total engagement count
     */
    public Long getTotalEngagement() {
        return (likesCount != null ? likesCount : 0L) +
                (commentsCount != null ? commentsCount : 0L) +
                (sharesCount != null ? sharesCount : 0L);
    }
}
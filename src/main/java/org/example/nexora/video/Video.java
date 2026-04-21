package org.example.nexora.video;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_videos_user_id", columnList = "userId"),
        @Index(name = "idx_videos_created_at", columnList = "createdAt"),
        @Index(name = "idx_videos_views", columnList = "views"),
        @Index(name = "idx_videos_likes", columnList = "likes"),
        @Index(name = "idx_videos_status", columnList = "status")
})
public class Video extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "video_format")
    private String videoFormat;

    @Column(name = "resolution_width")
    private Integer resolutionWidth;

    @Column(name = "resolution_height")
    private Integer resolutionHeight;

    @Column(name = "views", nullable = false)
    private Long views = 0L;

    @Column(name = "likes", nullable = false)
    private Long likes = 0L;

    @Column(name = "comments", nullable = false)
    private Long comments = 0L;

    @Column(name = "shares", nullable = false)
    private Long shares = 0L;

    @Column(name = "downloads", nullable = false)
    private Long downloads = 0L;

    @Column(name = "engagement_score", nullable = false)
    private Double engagementScore = 0.0;

    @Column(name = "creator_earnings", nullable = false)
    private Double creatorEarnings = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status = VideoStatus.PUBLISHED;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Column(name = "allow_comments", nullable = false)
    private Boolean allowComments = true;

    @Column(name = "allow_downloads", nullable = false)
    private Boolean allowDownloads = true;

    @Column(name = "content_moderation_flagged")
    private Boolean contentModerationFlagged = false;

    @Column(name = "content_moderation_reason")
    private String contentModerationReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public enum VideoStatus {
        DRAFT,
        PROCESSING,
        PUBLISHED,
        FLAGGED,
        REMOVED,
        PRIVATE
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (publishedAt == null && status == VideoStatus.PUBLISHED) {
            publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementViews() {
        this.views++;
        updateEngagementScore();
    }

    public void incrementLikes() {
        this.likes++;
        updateEngagementScore();
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
            updateEngagementScore();
        }
    }

    public void incrementComments() {
        this.comments++;
        updateEngagementScore();
    }

    public void decrementComments() {
        if (this.comments > 0) {
            this.comments--;
            updateEngagementScore();
        }
    }

    public void incrementShares() {
        this.shares++;
        updateEngagementScore();
    }

    public void incrementDownloads() {
        this.downloads++;
    }

    private void updateEngagementScore() {
        // Engagement score = (likes * 0.5) + (comments * 0.3) + (shares * 0.2)
        this.engagementScore = (likes * 0.5) + (comments * 0.3) + (shares * 0.2);
    }

    public void publish() {
        this.status = VideoStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void flagContent(String reason) {
        this.contentModerationFlagged = true;
        this.contentModerationReason = reason;
        this.status = VideoStatus.FLAGGED;
    }

    public void unflagContent() {
        this.contentModerationFlagged = false;
        this.contentModerationReason = null;
        this.status = VideoStatus.PUBLISHED;
    }
}
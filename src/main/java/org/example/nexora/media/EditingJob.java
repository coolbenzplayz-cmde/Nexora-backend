package org.example.nexora.media;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_editing_jobs", indexes = {
        @Index(name = "idx_media_jobs_user", columnList = "user_id"),
        @Index(name = "idx_media_jobs_status", columnList = "status"),
        @Index(name = "idx_media_jobs_scheduled", columnList = "scheduled_at"),
        @Index(name = "idx_media_jobs_template", columnList = "template_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EditingJob extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private EditingJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EditingJobStatus status = EditingJobStatus.QUEUED;

    @Column(name = "source_uri", length = 1024)
    private String sourceUri;

    @Column(name = "result_uri", length = 1024)
    private String resultUri;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "editing_config", columnDefinition = "JSON")
    private String editingConfig;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "publishing_platforms", length = 500)
    private String publishingPlatforms;

    @Column(name = "monetization_enabled", nullable = false)
    private Boolean monetizationEnabled = false;

    @Column(name = "monetization_settings", columnDefinition = "JSON")
    private String monetizationSettings;

    @Column(name = "collaboration_enabled", nullable = false)
    private Boolean collaborationEnabled = false;

    @Column(name = "version_count")
    private Integer versionCount = 0;

    @Column(name = "current_version")
    private Integer currentVersion = 1;

    @Column(name = "thumbnail_uri", length = 1024)
    private String thumbnailUri;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "format")
    private String format;

    @Column(name = "quality")
    private String quality;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "share_count")
    private Long shareCount = 0L;

    @Column(name = "download_count")
    private Long downloadCount = 0L;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public enum EditingJobType {
        IMAGE,
        VIDEO,
        AUDIO,
        CAROUSEL,
        STORY,
        REEL,
        THUMBNAIL,
        BANNER,
        COLLAGE,
        MEME,
        GIF
    }

    public enum EditingJobStatus {
        QUEUED,
        PROCESSING,
        COMPLETED,
        FAILED,
        SCHEDULED,
        PUBLISHED,
        EXPIRED,
        CANCELLED
    }

    // Helper methods for engagement tracking
    public void incrementViews() {
        this.viewCount++;
    }

    public void incrementLikes() {
        this.likeCount++;
    }

    public void incrementShares() {
        this.shareCount++;
    }

    public void incrementDownloads() {
        this.downloadCount++;
    }

    public boolean isPublished() {
        return status == EditingJobStatus.PUBLISHED;
    }

    public boolean isScheduled() {
        return status == EditingJobStatus.SCHEDULED;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}

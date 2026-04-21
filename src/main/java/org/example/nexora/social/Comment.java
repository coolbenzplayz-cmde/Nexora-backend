package org.example.nexora.social;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;
import org.example.nexora.video.Video;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "SocialComment")
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_video_id", columnList = "videoId"),
        @Index(name = "idx_comments_post_id", columnList = "postId"),
        @Index(name = "idx_comments_user_id", columnList = "userId"),
        @Index(name = "idx_comments_parent_id", columnList = "parentCommentId"),
        @Index(name = "idx_comments_created_at", columnList = "createdAt"),
        @Index(name = "idx_comments_content_type", columnList = "contentType")
})
public class Comment extends BaseEntity {

    @Column(name = "video_id")
    private Long videoId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "replies_count", nullable = false)
    private Integer repliesCount = 0;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Column(name = "content_moderation_flagged")
    private Boolean contentModerationFlagged = false;

    @Column(name = "content_moderation_reason")
    private String contentModerationReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum ContentType {
        VIDEO,
        POST
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementRepliesCount() {
        this.repliesCount++;
    }

    public void decrementRepliesCount() {
        if (this.repliesCount > 0) {
            this.repliesCount--;
        }
    }

    public void incrementLikesCount() {
        this.likesCount++;
    }

    public void decrementLikesCount() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.content = "[deleted]";
    }

    public void editContent(String newContent) {
        this.content = newContent;
        this.isEdited = true;
    }

    public void flagContent(String reason) {
        this.contentModerationFlagged = true;
        this.contentModerationReason = reason;
    }

    public void unflagContent() {
        this.contentModerationFlagged = false;
        this.contentModerationReason = null;
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public boolean isTopLevel() {
        return parentCommentId == null;
    }
}
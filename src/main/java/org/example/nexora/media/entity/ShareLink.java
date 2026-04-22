package org.example.nexora.media.entity;

import org.example.nexora.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_links", indexes = {
        @Index(name = "idx_share_links_job", columnList = "editing_job_id"),
        @Index(name = "idx_share_links_user", columnList = "user_id"),
        @Index(name = "idx_share_links_token", columnList = "token"),
        @Index(name = "idx_share_links_expires", columnList = "expires_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ShareLink extends BaseEntity {

    @Column(name = "editing_job_id", nullable = false)
    private Long editingJobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 20)
    private ContentCollaboration.CollaborationPermission permission;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "allow_download", nullable = false)
    private Boolean allowDownload = false;

    @Column(name = "allow_comment", nullable = false)
    private Boolean allowComment = false;

    @Column(name = "max_views")
    private Integer maxViews;

    @Column(name = "current_views")
    private Integer currentViews = 0;

    @Column(name = "custom_message", length = 500)
    private String customMessage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "access_count")
    private Long accessCount = 0L;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isViewLimitReached() {
        return maxViews != null && currentViews >= maxViews;
    }

    public boolean isValid() {
        return isActive && !isExpired() && !isViewLimitReached();
    }
}

package org.example.nexora.media;

import lombok.Data;
import org.example.nexora.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Share link entity
 */
@Entity
@Table(name = "share_links")
@Data
public class ShareLink extends BaseEntity {
    
    @Column(name = "content_id", nullable = false)
    private Long contentId;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(name = "share_url")
    private String shareUrl;
    
    @Column(nullable = false)
    private String permission; // VIEW, EDIT, COMMENT, DOWNLOAD
    
    @Column(name = "password_protected")
    private boolean passwordProtected = false;
    
    private String password;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "max_downloads")
    private Integer maxDownloads;
    
    @Column(name = "download_count")
    private int downloadCount = 0;
    
    @Column(name = "view_count")
    private int viewCount = 0;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (token == null) {
            token = java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean hasDownloadLimit() {
        return maxDownloads != null && downloadCount >= maxDownloads;
    }
}

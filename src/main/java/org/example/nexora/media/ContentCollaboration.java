package org.example.nexora.media;

import lombok.Data;
import org.example.nexora.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Content collaboration entity
 */
@Entity
@Table(name = "content_collaborations")
@Data
public class ContentCollaboration extends BaseEntity {
    
    @Column(name = "content_id", nullable = false)
    private Long contentId;
    
    @Column(name = "collaborator_id", nullable = false)
    private Long collaboratorId;
    
    @Column(nullable = false)
    private String role; // EDITOR, VIEWER, COMMENTER, ADMIN
    
    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, DECLINED, REVOKED
    
    @Column(name = "invited_by", nullable = false)
    private Long invitedBy;
    
    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions; // JSON data for specific permissions
    
    @Column(name = "access_token")
    private String accessToken;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (invitedAt == null) {
            invitedAt = LocalDateTime.now();
        }
    }
}

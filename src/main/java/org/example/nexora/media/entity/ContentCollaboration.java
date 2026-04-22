package org.example.nexora.media.entity;

import org.example.nexora.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_collaborations", indexes = {
        @Index(name = "idx_collaborations_job", columnList = "editing_job_id"),
        @Index(name = "idx_collaborations_owner", columnList = "owner_id"),
        @Index(name = "idx_collaborations_collaborator", columnList = "collaborator_id"),
        @Index(name = "idx_collaborations_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ContentCollaboration extends BaseEntity {

    @Column(name = "editing_job_id", nullable = false)
    private Long editingJobId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "collaborator_id", nullable = false)
    private Long collaboratorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 20)
    private CollaborationPermission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CollaborationStatus status = CollaborationStatus.PENDING;

    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt = LocalDateTime.now();

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "access_token", length = 255)
    private String accessToken;

    @Column(name = "message", length = 500)
    private String message;

    public enum CollaborationPermission {
        VIEW,
        EDIT,
        COMMENT,
        ADMIN
    }

    public enum CollaborationStatus {
        PENDING,
        ACTIVE,
        DECLINED,
        EXPIRED,
        REVOKED
    }
}

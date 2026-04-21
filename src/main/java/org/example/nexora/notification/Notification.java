package org.example.nexora.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.util.UUID;

/**
 * Notification entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "userId"),
        @Index(name = "idx_notifications_is_read", columnList = "isRead")
})
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private java.time.LocalDateTime readAt;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    public enum NotificationType {
        LIKE,
        COMMENT,
        FOLLOW,
        SHARE,
        MENTION,
        MESSAGE,
        TIP,
        SUBSCRIPTION,
        PAYMENT,
        SYSTEM
    }

    // Explicit getters and setters to ensure they exist
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public java.time.LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(java.time.LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        // BaseEntity handles this automatically
    }

    /**
     * Notification priority levels
     */
    public enum NotificationPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}

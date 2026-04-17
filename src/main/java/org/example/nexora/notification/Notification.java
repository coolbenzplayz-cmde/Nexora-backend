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
}
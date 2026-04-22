package org.example.nexora.media.entity;

import org.example.nexora.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_analytics", indexes = {
        @Index(name = "idx_analytics_job", columnList = "editing_job_id"),
        @Index(name = "idx_analytics_user", columnList = "user_id"),
        @Index(name = "idx_analytics_event", columnList = "event_type"),
        @Index(name = "idx_analytics_timestamp", columnList = "timestamp")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ContentAnalytics extends BaseEntity {

    @Column(name = "editing_job_id", nullable = false)
    private Long editingJobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AnalyticsEventType eventType;

    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "metrics", columnDefinition = "JSON")
    private String metrics;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "referrer", length = 500)
    private String referrer;

    @Column(name = "geolocation", length = 100)
    private String geolocation;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    public enum AnalyticsEventType {
        VIEW,
        LIKE,
        COMMENT,
        SHARE,
        DOWNLOAD,
        EDIT_START,
        EDIT_COMPLETE,
        EXPORT,
        TEMPLATE_USED,
        COLLABORATION_INVITE,
        COLLABORATION_ACCEPT,
        VERSION_CREATED,
        SCHEDULE_PUBLISH,
        MONETIZATION_EARNED
    }
}

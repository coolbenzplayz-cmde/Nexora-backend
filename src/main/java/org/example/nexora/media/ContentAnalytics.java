package org.example.nexora.media;

import lombok.Data;
import org.example.nexora.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Content analytics entity
 */
@Entity
@Table(name = "content_analytics")
@Data
public class ContentAnalytics extends BaseEntity {
    
    @Column(name = "content_id", nullable = false)
    private Long contentId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false)
    private String eventType; // VIEW, LIKE, COMMENT, SHARE, DOWNLOAD
    
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData; // JSON data for additional event information
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "referrer")
    private String referrer;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

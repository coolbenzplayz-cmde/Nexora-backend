package org.example.nexora.notification;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Statistics for notifications
 */
@Data
public class NotificationStatistics {
    
    private Long totalNotifications;
    private Long readNotifications;
    private Long unreadNotifications;
    private LocalDateTime lastNotificationTime;
    private Map<String, Long> notificationsByType;
    private Double averageReadTime;
    
    public NotificationStatistics() {
        this.totalNotifications = 0L;
        this.readNotifications = 0L;
        this.unreadNotifications = 0L;
        this.averageReadTime = 0.0;
    }
}

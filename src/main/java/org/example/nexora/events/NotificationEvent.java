package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Notification domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends DomainEvent {
    
    private Long notificationId;
    private Long userId;
    private String action; // SENT, DELIVERED, READ, DISMISSED
    private String type; // EMAIL, PUSH, SMS, IN_APP
    private String title;
    private String message;
    private boolean read;
    
    public NotificationEvent() {
        super("NOTIFICATION_EVENT", "NOTIFICATION_SERVICE");
    }
    
    public NotificationEvent(Long notificationId, Long userId, String action) {
        this();
        this.notificationId = notificationId;
        this.userId = userId;
        this.action = action;
    }
}

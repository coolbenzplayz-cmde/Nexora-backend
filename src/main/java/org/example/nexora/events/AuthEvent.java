package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Authentication domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthEvent extends DomainEvent {
    
    private Long userId;
    private String username;
    private String action; // LOGIN, LOGOUT, REGISTER, PASSWORD_CHANGE, TOKEN_REFRESH
    private String ipAddress;
    private String userAgent;
    private boolean success;
    
    public AuthEvent() {
        super("AUTH_EVENT", "AUTH_SERVICE");
    }
    
    public AuthEvent(Long userId, String username, String action, boolean success) {
        this();
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.success = success;
    }
}

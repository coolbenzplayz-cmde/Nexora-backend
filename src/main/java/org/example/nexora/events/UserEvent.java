package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserEvent extends DomainEvent {
    
    private Long userId;
    private String username;
    private String email;
    private String action; // CREATED, UPDATED, DELETED, LOGIN, LOGOUT
    private String details;
    
    public UserEvent() {
        super("USER_EVENT", "USER_SERVICE");
    }
    
    public UserEvent(Long userId, String username, String action) {
        this();
        this.userId = userId;
        this.username = username;
        this.action = action;
    }
}

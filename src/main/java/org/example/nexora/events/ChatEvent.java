package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Chat domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatEvent extends DomainEvent {
    
    private Long conversationId;
    private Long userId;
    private String message;
    private String action; // MESSAGE_SENT, MESSAGE_RECEIVED, CONVERSATION_CREATED, USER_JOINED, USER_LEFT
    private String messageType; // TEXT, IMAGE, VIDEO, VOICE, FILE
    
    public ChatEvent() {
        super("CHAT_EVENT", "CHAT_SERVICE");
    }
    
    public ChatEvent(Long conversationId, Long userId, String action) {
        this();
        this.conversationId = conversationId;
        this.userId = userId;
        this.action = action;
    }
}

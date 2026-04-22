package org.example.nexora.messaging;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Conversation participant entity
 */
@Data
public class ConversationParticipant {
    
    private Long id;
    private Long conversationId;
    private Long userId;
    private ParticipantRole role;
    private LocalDateTime joinedAt;
    private boolean isActive;
    private String displayName;
    
    public ConversationParticipant() {
        this.role = ParticipantRole.MEMBER;
        this.joinedAt = LocalDateTime.now();
        this.isActive = true;
    }
}

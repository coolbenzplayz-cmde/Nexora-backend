package org.example.nexora.messaging;

import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Conversation lock entity for privacy and moderation
 */
@Data
public class ConversationLock {
    
    private Long conversationId;
    private Long lockedBy;
    private String lockType; // "MODERATION", "PRIVACY", "SYSTEM"
    private String reason;
    private LocalDateTime lockedAt;
    private Duration duration;
    private Duration maxDuration;
    private boolean active;
    
    public ConversationLock() {
        this.lockedAt = LocalDateTime.now();
        this.active = true;
        this.duration = Duration.ofHours(1);
        this.maxDuration = Duration.ofHours(24);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(lockedAt.plus(duration));
    }
}

package org.example.nexora.messaging;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Conversation entity
 */
@Data
public class Conversation {
    
    private Long id;
    private String title;
    private String type; // PRIVATE, GROUP, CHANNEL
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ConversationParticipant> participants;
    private boolean isActive;
    private String description;
    private String avatarUrl;
    
    public Conversation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.type = "PRIVATE";
    }
    
    public boolean isGroup() {
        return "GROUP".equals(type);
    }
    
    public boolean isChannel() {
        return "CHANNEL".equals(type);
    }
    
    public int getParticipantCount() {
        return participants != null ? participants.size() : 0;
    }
}

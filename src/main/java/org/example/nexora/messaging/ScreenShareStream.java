package org.example.nexora.messaging;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Screen share stream entity
 */
@Data
public class ScreenShareStream {
    
    private String streamId;
    private Long userId;
    private String callId;
    private LocalDateTime startedAt;
    private boolean active;
    private String streamUrl;
    private String quality; // LOW, MEDIUM, HIGH, ULTRA
    
    public ScreenShareStream() {
        this.startedAt = LocalDateTime.now();
        this.active = true;
        this.quality = "MEDIUM";
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void stopStream() {
        this.active = false;
    }
}

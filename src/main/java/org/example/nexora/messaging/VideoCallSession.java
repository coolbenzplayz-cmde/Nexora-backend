package org.example.nexora.messaging;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Video call session entity
 */
@Data
public class VideoCallSession {
    
    private String sessionId;
    private Long callerId;
    private Long calleeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // INITIATING, CONNECTED, ENDED, FAILED
    private int duration;
    private String callType; // VIDEO, AUDIO, SCREEN_SHARE
    private List<String> participants;
    private String roomName;
    
    public VideoCallSession() {
        this.startTime = LocalDateTime.now();
        this.status = "INITIATING";
        this.duration = 0;
        this.callType = "VIDEO";
    }
    
    public boolean isActive() {
        return "CONNECTED".equals(status);
    }
    
    public long getDurationInSeconds() {
        if (endTime != null) {
            return java.time.Duration.between(startTime, endTime).getSeconds();
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }
    
    public boolean isScreenShareEnabled() {
        return "SCREEN_SHARE".equals(callType) || participants != null && participants.size() > 2;
    }
}

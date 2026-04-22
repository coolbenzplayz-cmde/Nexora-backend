package org.example.nexora.messaging;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Call token for authentication
 */
@Data
public class CallToken {
    
    private Long userId;
    private String callId;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<CallPermission> permissions;
    
    public CallToken() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

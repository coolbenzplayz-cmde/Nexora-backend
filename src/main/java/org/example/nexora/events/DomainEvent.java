package org.example.nexora.events;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Base domain event class
 */
@Data
public abstract class DomainEvent {
    
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private String version;
    
    public DomainEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
        this.version = "1.0";
    }
    
    public DomainEvent(String eventType, String source) {
        this();
        this.eventType = eventType;
        this.source = source;
    }
}

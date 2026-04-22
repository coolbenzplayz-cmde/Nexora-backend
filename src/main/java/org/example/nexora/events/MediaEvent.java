package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Media domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MediaEvent extends DomainEvent {
    
    private Long mediaId;
    private Long userId;
    private String action; // UPLOADED, DELETED, VIEWED, LIKED, COMMENTED, SHARED
    private String mediaType; // IMAGE, VIDEO, AUDIO, DOCUMENT
    private String title;
    private String description;
    
    public MediaEvent() {
        super("MEDIA_EVENT", "MEDIA_SERVICE");
    }
    
    public MediaEvent(Long mediaId, Long userId, String action) {
        this();
        this.mediaId = mediaId;
        this.userId = userId;
        this.action = action;
    }
}

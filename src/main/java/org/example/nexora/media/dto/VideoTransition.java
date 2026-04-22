package org.example.nexora.media.dto;

import lombok.Data;

/**
 * Video transition DTO
 */
@Data
public class VideoTransition {
    
    private String transitionType; // FADE, SLIDE, ZOOM, DISSOLVE, WIPE
    private double duration; // in seconds
    private String direction; // LEFT, RIGHT, UP, DOWN, IN, OUT
    private boolean enabled;
    
    public VideoTransition() {
        this.duration = 1.0;
        this.enabled = true;
        this.direction = "RIGHT";
    }
    
    public VideoTransition(String transitionType, double duration) {
        this.transitionType = transitionType;
        this.duration = duration;
        this.enabled = true;
        this.direction = "RIGHT";
    }
}

package org.example.nexora.media.dto;

import lombok.Data;

/**
 * Text overlay DTO
 */
@Data
public class TextOverlay {
    
    private String text;
    private String fontFamily;
    private int fontSize;
    private String color; // HEX color code
    private double x; // X position (0.0 to 1.0)
    private double y; // Y position (0.0 to 1.0)
    private int startTime; // in seconds
    private int duration; // in seconds
    private boolean enabled;
    
    public TextOverlay() {
        this.fontSize = 24;
        this.color = "#FFFFFF";
        this.x = 0.5;
        this.y = 0.5;
        this.startTime = 0;
        this.duration = 5;
        this.enabled = true;
    }
    
    public TextOverlay(String text, int startTime, int duration) {
        this.text = text;
        this.startTime = startTime;
        this.duration = duration;
        this.fontSize = 24;
        this.color = "#FFFFFF";
        this.x = 0.5;
        this.y = 0.5;
        this.enabled = true;
    }
}

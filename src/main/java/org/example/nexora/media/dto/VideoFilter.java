package org.example.nexora.media.dto;

import lombok.Data;

/**
 * Video filter DTO
 */
@Data
public class VideoFilter {
    
    private String filterType; // BRIGHTNESS, CONTRAST, SATURATION, BLUR, SHARPEN
    private double intensity; // 0.0 to 1.0
    private String name;
    private boolean enabled;
    
    public VideoFilter() {
        this.intensity = 0.5;
        this.enabled = true;
    }
    
    public VideoFilter(String filterType, double intensity) {
        this.filterType = filterType;
        this.intensity = intensity;
        this.enabled = true;
    }
}

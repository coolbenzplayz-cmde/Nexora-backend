package org.example.nexora.messaging;

import lombok.Data;

/**
 * Screen share configuration
 */
@Data
public class ScreenShareConfig {
    
    private String resolution; // 720p, 1080p, 4K
    private int frameRate; // 30, 60, 120
    private String quality; // LOW, MEDIUM, HIGH, ULTRA
    private boolean includeAudio;
    private int maxBitrate;
    
    public ScreenShareConfig() {
        this.resolution = "1080p";
        this.frameRate = 30;
        this.quality = "MEDIUM";
        this.includeAudio = false;
        this.maxBitrate = 2000; // kbps
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public int getFrameRate() {
        return frameRate;
    }
    
    public void setMaxBitrate(int maxBitrate) {
        this.maxBitrate = maxBitrate;
    }
}

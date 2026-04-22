package org.example.nexora.media.dto;

import lombok.Data;

/**
 * Audio effect DTO
 */
@Data
public class AudioEffect {
    
    private String effectType; // ECHO, REVERB, DISTORTION, PITCH_SHIFT, VOLUME
    private double intensity; // 0.0 to 1.0
    private String name;
    private boolean enabled;
    
    public AudioEffect() {
        this.intensity = 0.5;
        this.enabled = true;
    }
    
    public AudioEffect(String effectType, double intensity) {
        this.effectType = effectType;
        this.intensity = intensity;
        this.enabled = true;
    }
}

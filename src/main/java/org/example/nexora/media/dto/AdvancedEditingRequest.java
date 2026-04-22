package org.example.nexora.media.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AdvancedEditingRequest {
    private List<AdvancedEffect> advancedEffects;
    private List<VideoTransition> transitions;
    private List<AudioEnhancement> audioEnhancements;
    private ColorGrading colorGrading;
    private List<TextOverlay> textOverlays;
    private List<StickerOverlay> stickerOverlays;
    private BackgroundRemoval backgroundRemoval;
    private FaceEnhancement faceEnhancement;
    private MotionTracking motionTracking;
    
    @Data
    public static class AdvancedEffect {
        private String type;
        private Map<String, Object> parameters;
        private Double intensity;
        private Boolean enabled = true;
    }
    
    @Data
    public static class VideoTransition {
        private String type;
        private Double duration;
        private Map<String, Object> parameters;
    }
    
    @Data
    public static class AudioEnhancement {
        private String type;
        private Map<String, Object> parameters;
        private Double intensity;
    }
    
    @Data
    public static class ColorGrading {
        private Double brightness;
        private Double contrast;
        private Double saturation;
        private Double hue;
        private Double temperature;
        private Double tint;
        private Double shadows;
        private Double highlights;
        private String lutUri;
    }
    
    @Data
    public static class TextOverlay {
        private String text;
        private String font;
        private Integer fontSize;
        private String color;
        private Double x;
        private Double y;
        private Double rotation;
        private Double opacity;
        private String animation;
        private Map<String, Object> style;
    }
    
    @Data
    public static class StickerOverlay {
        private String stickerUri;
        private Double x;
        private Double y;
        private Double scale;
        private Double rotation;
        private Double opacity;
        private String animation;
    }
    
    @Data
    public static class BackgroundRemoval {
        private Boolean enabled = false;
        private String newBackgroundUri;
        private Boolean blurBackground = false;
        private Double blurIntensity;
    }
    
    @Data
    public static class FaceEnhancement {
        private Boolean enabled = false;
        private Boolean skinSmoothing = false;
        private Double skinSmoothingIntensity;
        private Boolean teethWhitening = false;
        private Boolean eyeEnhancement = false;
        private Boolean faceSlimming = false;
    }
    
    @Data
    public static class MotionTracking {
        private Boolean enabled = false;
        private String trackingType;
        private Map<String, Object> trackingData;
    }
}

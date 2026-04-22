package org.example.nexora.media.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PhotoEditingRequest {
    private String sourceUri;
    private List<ImageFilter> filters;
    private ImageAdjustments adjustments;
    private OutputSettings outputSettings;
    
    @Data
    public static class ImageFilter {
        private String type;
        private Double intensity;
        private Map<String, Object> parameters;
    }
    
    @Data
    public static class ImageAdjustments {
        private Double brightness;
        private Double contrast;
        private Double saturation;
        private Double hue;
        private Double exposure;
        private Double shadows;
        private Double highlights;
        private Double temperature;
        private Double tint;
    }
    
    @Data
    public static class OutputSettings {
        private String format;
        private Integer width;
        private Integer height;
        private Integer quality;
        private Boolean preserveAspectRatio;
    }
}

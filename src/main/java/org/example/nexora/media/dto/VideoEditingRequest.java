package org.example.nexora.media.dto;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class VideoEditingRequest {
    private String sourceUri;
    private Map<String, Object> editingConfig;
    private VideoEffects videoEffects;
    private AudioSettings audioSettings;
    private OutputSettings outputSettings;
    
    @Data
    public static class VideoEffects {
        private Boolean trim;
        private Double startTime;
        private Double endTime;
        private Double speed;
        private List<VideoFilter> filters;
        private List<VideoTransition> transitions;
        private List<TextOverlay> textOverlays;
    }
    
    @Data
    public static class AudioSettings {
        private Double volume;
        private Boolean mute;
        private String backgroundMusicUri;
        private List<AudioEffect> effects;
    }
    
    @Data
    public static class OutputSettings {
        private String format;
        private Integer width;
        private Integer height;
        private Integer frameRate;
        private Integer bitrate;
        private String quality;
    }
}

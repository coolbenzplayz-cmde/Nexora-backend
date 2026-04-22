package org.example.nexora.media.dto;

import lombok.Data;
import java.util.Map;

@Data
public class VideoEffect {
    private String type;
    private Map<String, Object> parameters;
    private Double intensity;
    private Double startTime;
    private Double endTime;
    private Boolean enabled = true;
}

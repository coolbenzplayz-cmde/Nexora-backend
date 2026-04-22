package org.example.nexora.media.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ImageFilter {
    private String type;
    private Map<String, Object> parameters;
    private Double intensity;
    private Boolean enabled = true;
}

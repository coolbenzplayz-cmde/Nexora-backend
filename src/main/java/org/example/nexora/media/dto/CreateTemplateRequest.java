package org.example.nexora.media.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateTemplateRequest {
    private String name;
    private String description;
    private String templateType;
    private String thumbnailUri;
    private Map<String, Object> templateData;
    private Boolean isPublic = false;
    private String category;
    private String[] tags;
}

package org.example.nexora.media.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateFromTemplateRequest {
    private Long templateId;
    private String sourceUri;
    private Map<String, Object> customizations;
    private String name;
    private String description;
    private String[] tags;
}

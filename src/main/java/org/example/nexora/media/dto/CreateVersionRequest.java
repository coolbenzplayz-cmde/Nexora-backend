package org.example.nexora.media.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CreateVersionRequest {
    private Integer versionNumber;
    private String contentUri;
    private String changes;
    private Map<String, Object> metadata;
    private String thumbnailUri;
    private Long fileSize;
    private Double duration;
    private String dimensions;
    private String format;
}

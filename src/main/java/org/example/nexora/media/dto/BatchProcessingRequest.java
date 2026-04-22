package org.example.nexora.media.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BatchProcessingRequest {
    private List<String> fileUris;
    private List<BatchOperation> operations;
    private BatchSettings settings;
    
    @Data
    public static class BatchOperation {
        private String type;
        private Map<String, Object> parameters;
        private Boolean enabled = true;
    }
    
    @Data
    public static class BatchSettings {
        private String outputFormat;
        private String outputQuality;
        private String outputDirectory;
        private Boolean preserveOriginal = true;
        private Boolean generateThumbnails = false;
        private Boolean addWatermark = false;
        private String watermarkUri;
        private Map<String, Object> watermarkSettings;
    }
}

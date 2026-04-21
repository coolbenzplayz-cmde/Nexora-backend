package org.example.nexora.risk;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request for updating risk model
 */
@Data
public class ModelUpdateRequest {
    
    private String modelType;
    private List<RiskTrainingData> trainingData;
    private LocalDateTime updateTimestamp;
    private String updateReason;
    private Double accuracyThreshold;
    
    public ModelUpdateRequest() {
        this.updateTimestamp = LocalDateTime.now();
        this.accuracyThreshold = 0.95;
    }
}

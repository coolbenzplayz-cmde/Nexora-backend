package org.example.nexora.risk;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data class for risk model training
 */
@Data
public class RiskTrainingData {
    
    private Long userId;
    private BigDecimal amount;
    private String transactionType;
    private Integer riskScore;
    private LocalDateTime timestamp;
    private String outcome; // "approved", "rejected", "flagged"
    private Map<String, Object> features;
    
    public RiskTrainingData() {
        this.timestamp = LocalDateTime.now();
        this.riskScore = 0;
    }
}

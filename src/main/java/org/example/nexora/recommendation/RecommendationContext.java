package org.example.nexora.recommendation;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Context for recommendation calculations
 */
@Data
public class RecommendationContext {
    
    private Long userId;
    private String deviceType;
    private String location;
    private LocalDateTime currentTime;
    private Set<String> userInterests;
    private Map<String, Double> categoryPreferences;
    private Double personalizationWeight;
    private String recommendationType; // "video", "user", "content"
    
    public RecommendationContext() {
        this.currentTime = LocalDateTime.now();
        this.personalizationWeight = 0.7;
        this.recommendationType = "video";
    }
}

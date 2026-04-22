package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AIEvent extends DomainEvent {
    
    private Long userId;
    private String action; // MODEL_TRAINING, PREDICTION_MADE, INSIGHT_GENERATED, RECOMMENDATION_PROVIDED
    private String modelType; // CLASSIFICATION, REGRESSION, CLUSTERING, NLP, COMPUTER_VISION
    private String input;
    private String output;
    private double confidence;
    
    public AIEvent() {
        super("AI_EVENT", "AI_SERVICE");
    }
    
    public AIEvent(Long userId, String action, String modelType) {
        this();
        this.userId = userId;
        this.action = action;
        this.modelType = modelType;
    }
}

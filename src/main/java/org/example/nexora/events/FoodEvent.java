package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Food domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FoodEvent extends DomainEvent {
    
    private Long orderId;
    private Long restaurantId;
    private Long customerId;
    private String action; // ORDER_PLACED, ORDER_CONFIRMED, ORDER_PREPARING, ORDER_READY, ORDER_DELIVERED, ORDER_CANCELLED
    private String status;
    private String items;
    
    public FoodEvent() {
        super("FOOD_EVENT", "FOOD_SERVICE");
    }
    
    public FoodEvent(Long orderId, String action) {
        this();
        this.orderId = orderId;
        this.action = action;
    }
}

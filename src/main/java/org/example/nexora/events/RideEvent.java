package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Ride domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RideEvent extends DomainEvent {
    
    private Long rideId;
    private Long driverId;
    private Long passengerId;
    private String action; // RIDE_REQUESTED, RIDE_ACCEPTED, RIDE_STARTED, RIDE_COMPLETED, RIDE_CANCELLED
    private String pickupLocation;
    private String dropoffLocation;
    private String status;
    
    public RideEvent() {
        super("RIDE_EVENT", "RIDE_SERVICE");
    }
    
    public RideEvent(Long rideId, String action) {
        this();
        this.rideId = rideId;
        this.action = action;
    }
}

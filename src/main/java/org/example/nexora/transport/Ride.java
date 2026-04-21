package org.example.nexora.transport;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ride entity for ride-hailing service
 */
@Data
public class Ride {
    
    private Long id;
    private Long userId;
    private Long driverId;
    private String pickupLocation;
    private String destination;
    private BigDecimal fare;
    private String status; // PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private String paymentId;
    private String rating;
    
    public Ride() {
        this.status = "PENDING";
        this.requestedAt = LocalDateTime.now();
        this.fare = BigDecimal.ZERO;
    }
}

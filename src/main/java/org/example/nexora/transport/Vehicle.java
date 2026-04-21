package org.example.nexora.transport;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Vehicle entity for ride-hailing service
 */
@Data
public class Vehicle {
    
    private Long id;
    private String make;
    private String model;
    private String licensePlate;
    private String type; // CAR, SUV, BIKE, SCOOTER
    private int capacity;
    private String color;
    private Long driverId;
    private boolean available;
    private LocalDateTime createdAt;
    
    public Vehicle() {
        this.available = true;
        this.capacity = 4;
        this.createdAt = LocalDateTime.now();
    }
}

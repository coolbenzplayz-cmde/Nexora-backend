package org.example.nexora.transport;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Driver entity for ride-hailing service
 */
@Data
public class Driver {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private String vehicleType;
    private String currentLocation;
    private boolean available;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    
    public Driver() {
        this.available = true;
        this.rating = BigDecimal.valueOf(4.5);
        this.createdAt = LocalDateTime.now();
    }
}

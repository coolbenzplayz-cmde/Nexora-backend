package org.example.nexora.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDTO {
    private Long id;
    private String pickupLocation;
    private String dropoffLocation;
    private BigDecimal estimatedPrice;
    private String status;
    private String driverId;
    private String passengerId;
}

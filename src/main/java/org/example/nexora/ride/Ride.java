package org.example.nexora.ride;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.util.UUID;

/**
 * Ride entity for ride-sharing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rides")
public class Ride extends BaseEntity {

    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "dropoff_location")
    private String dropoffLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status = RideStatus.REQUESTED;

    @Column(name = "fare", precision = 10, scale = 2)
    private Double fare;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "vehicle_type")
    private String vehicleType;

    public enum RideStatus {
        REQUESTED,
        ACCEPTED,
        ARRIVED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
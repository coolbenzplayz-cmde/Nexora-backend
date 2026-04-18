package org.example.nexora.ride;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ride_requests")
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RideStatus status = RideStatus.PENDING;

    @Column(name = "pickup_location", length = 500)
    private String pickupLocation;

    @Column(name = "pickup_latitude")
    private Double pickupLatitude;

    @Column(name = "pickup_longitude")
    private Double pickupLongitude;

    @Column(name = "dropoff_location", length = 500)
    private String dropoffLocation;

    @Column(name = "dropoff_latitude")
    private Double dropoffLatitude;

    @Column(name = "dropoff_longitude")
    private Double dropoffLongitude;

    @Column(name = "estimated_fare", precision = 10, scale = 2)
    private BigDecimal estimatedFare;

    @Column(name = "actual_fare", precision = 10, scale = 2)
    private BigDecimal actualFare;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "driver_accept_time")
    private LocalDateTime driverAcceptTime;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "dropoff_time")
    private LocalDateTime dropoffTime;

    @Column(name = "driver_rating")
    private Integer driverRating;

    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "driver_comment")
    private String driverComment;

    @Column(name = "user_comment")
    private String userComment;

    public enum RideStatus {
        PENDING,
        DRIVER_ASSIGNED,
        ARRIVING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}

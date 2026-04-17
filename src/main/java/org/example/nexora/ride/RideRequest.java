package org.example.nexora.ride;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public RideRequest() {}

    public RideRequest(Long userId, String pickupLocation, String dropoffLocation) {
        this.userId = userId;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.status = RideStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }

    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }

    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }

    public Double getDropoffLatitude() { return dropoffLatitude; }
    public void setDropoffLatitude(Double dropoffLatitude) { this.dropoffLatitude = dropoffLatitude; }

    public Double getDropoffLongitude() { return dropoffLongitude; }
    public void setDropoffLongitude(Double dropoffLongitude) { this.dropoffLongitude = dropoffLongitude; }

    public BigDecimal getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(BigDecimal estimatedFare) { this.estimatedFare = estimatedFare; }

    public BigDecimal getActualFare() { return actualFare; }
    public void setActualFare(BigDecimal actualFare) { this.actualFare = actualFare; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDriverAcceptTime() { return driverAcceptTime; }
    public void setDriverAcceptTime(LocalDateTime driverAcceptTime) { this.driverAcceptTime = driverAcceptTime; }

    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }

    public LocalDateTime getDropoffTime() { return dropoffTime; }
    public void setDropoffTime(LocalDateTime dropoffTime) { this.dropoffTime = dropoffTime; }

    public Integer getDriverRating() { return driverRating; }
    public void setDriverRating(Integer driverRating) { this.driverRating = driverRating; }

    public Integer getUserRating() { return userRating; }
    public void setUserRating(Integer userRating) { this.userRating = userRating; }

    public String getDriverComment() { return driverComment; }
    public void setDriverComment(String driverComment) { this.driverComment = driverComment; }

    public String getUserComment() { return userComment; }
    public void setUserComment(String userComment) { this.userComment = userComment; }
}

package org.example.nexora.ride;

import org.example.nexora.common.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RideService {

    private final RideRequestRepository rideRequestRepository;

    public RideService(RideRequestRepository rideRequestRepository) {
        this.rideRequestRepository = rideRequestRepository;
    }

    public RideRequest createRideRequest(RideRequest rideRequest) {
        // Calculate estimated fare based on distance
        if (rideRequest.getDistanceKm() != null) {
            BigDecimal fare = calculateFare(rideRequest.getDistanceKm(), rideRequest.getVehicleType());
            rideRequest.setEstimatedFare(fare);
        }
        rideRequest.setStatus(RideRequest.RideStatus.PENDING);
        rideRequest.setCreatedAt(LocalDateTime.now());
        return rideRequestRepository.save(rideRequest);
    }

    public RideRequest getRideRequestById(Long rideId) {
        return rideRequestRepository.findById(rideId)
                .orElseThrow(() -> new BusinessException("Ride request not found"));
    }

    public Page<RideRequest> getUserRides(Long userId, Pageable pageable) {
        return rideRequestRepository.findByUserId(userId, pageable);
    }

    public Page<RideRequest> getDriverRides(Long driverId, Pageable pageable) {
        return rideRequestRepository.findByDriverId(driverId, pageable);
    }

    public List<RideRequest> getPendingRides() {
        return rideRequestRepository.findByStatus(RideRequest.RideStatus.PENDING);
    }

    public RideRequest acceptRide(Long rideId, Long driverId) {
        RideRequest ride = rideRequestRepository.findById(rideId)
                .orElseThrow(() -> new BusinessException("Ride request not found"));
        
        if (ride.getStatus() != RideRequest.RideStatus.PENDING) {
            throw new BusinessException("This ride is no longer available");
        }
        
        ride.setDriverId(driverId);
        ride.setStatus(RideRequest.RideStatus.DRIVER_ASSIGNED);
        ride.setDriverAcceptTime(LocalDateTime.now());
        
        return rideRequestRepository.save(ride);
    }

    public RideRequest updateRideStatus(Long rideId, RideRequest.RideStatus newStatus) {
        RideRequest ride = rideRequestRepository.findById(rideId)
                .orElseThrow(() -> new BusinessException("Ride request not found"));
        
        ride.setStatus(newStatus);
        
        switch (newStatus) {
            case ARRIVING:
                // Driver is on the way
                break;
            case IN_PROGRESS:
                ride.setPickupTime(LocalDateTime.now());
                break;
            case COMPLETED:
                ride.setDropoffTime(LocalDateTime.now());
                if (ride.getActualFare() == null) {
                    ride.setActualFare(ride.getEstimatedFare());
                }
                break;
            case CANCELLED:
                // Handle cancellation logic
                break;
        }
        
        return rideRequestRepository.save(ride);
    }

    public RideRequest rateRide(Long rideId, Integer rating, String comment, boolean isDriverRating) {
        RideRequest ride = rideRequestRepository.findById(rideId)
                .orElseThrow(() -> new BusinessException("Ride request not found"));
        
        if (isDriverRating) {
            ride.setDriverRating(rating);
            ride.setDriverComment(comment);
        } else {
            ride.setUserRating(rating);
            ride.setUserComment(comment);
        }
        
        return rideRequestRepository.save(ride);
    }

    public RideRequest updateDriverLocation(Long rideId, Double latitude, Double longitude) {
        RideRequest ride = rideRequestRepository.findById(rideId)
                .orElseThrow(() -> new BusinessException("Ride request not found"));
        
        // Update driver's current location for tracking
        // This would typically be stored separately for real-time tracking
        
        return ride;
    }

    public RideRequest cancelRide(Long rideId, Long userId) {
        RideRequest ride = rideRequestRepository.findById(rideId)
                .orElseThrow(() -> new BusinessException("Ride request not found"));
        
        if (!ride.getUserId().equals(userId) && ride.getDriverId() == null) {
            throw new BusinessException("Not authorized to cancel this ride");
        }
        
        if (ride.getStatus() == RideRequest.RideStatus.IN_PROGRESS ||
            ride.getStatus() == RideRequest.RideStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel ride in progress or completed");
        }
        
        ride.setStatus(RideRequest.RideStatus.CANCELLED);
        
        return rideRequestRepository.save(ride);
    }

    public long getUserRideCount(Long userId) {
        return rideRequestRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();
    }

    public long getDriverRideCount(Long driverId) {
        return rideRequestRepository.findByDriverId(driverId, Pageable.unpaged()).getTotalElements();
    }

    private BigDecimal calculateFare(Double distanceKm, String vehicleType) {
        // Base fare + distance rate based on vehicle type
        BigDecimal baseFare = new BigDecimal("2.50");
        BigDecimal ratePerKm;
        
        if (vehicleType == null) {
            vehicleType = "standard";
        }
        
        switch (vehicleType.toLowerCase()) {
            case "premium":
                ratePerKm = new BigDecimal("2.00");
                break;
            case "van":
                ratePerKm = new BigDecimal("2.50");
                break;
            case "motorcycle":
                ratePerKm = new BigDecimal("1.00");
                break;
            default: // standard
                ratePerKm = new BigDecimal("1.50");
        }
        
        return baseFare.add(ratePerKm.multiply(BigDecimal.valueOf(distanceKm)));
    }
}

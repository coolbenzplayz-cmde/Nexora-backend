package org.example.nexora.ride;

import org.example.nexora.common.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<RideRequest> createRideRequest(@RequestBody RideRequest rideRequest) {
        return ResponseEntity.ok(rideService.createRideRequest(rideRequest));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideRequest> getRideRequest(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.getRideRequestById(rideId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<PaginationResponse<RideRequest>> getUserRides(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<RideRequest> rides = rideService.getUserRides(userId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(rides));
    }

    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<PaginationResponse<RideRequest>> getDriverRides(
            @PathVariable Long driverId,
            Pageable pageable) {
        Page<RideRequest> rides = rideService.getDriverRides(driverId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(rides));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RideRequest>> getPendingRides() {
        return ResponseEntity.ok(rideService.getPendingRides());
    }

    @PatchMapping("/{rideId}/accept")
    public ResponseEntity<RideRequest> acceptRide(
            @PathVariable Long rideId,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(rideService.acceptRide(rideId, driverId));
    }

    @PatchMapping("/{rideId}/status")
    public ResponseEntity<RideRequest> updateRideStatus(
            @PathVariable Long rideId,
            @RequestParam RideRequest.RideStatus status) {
        return ResponseEntity.ok(rideService.updateRideStatus(rideId, status));
    }

    @PatchMapping("/{rideId}/rate")
    public ResponseEntity<RideRequest> rateRide(
            @PathVariable Long rideId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @RequestParam boolean isDriverRating) {
        return ResponseEntity.ok(rideService.rateRide(rideId, rating, comment, isDriverRating));
    }

    @PatchMapping("/{rideId}/location")
    public ResponseEntity<RideRequest> updateDriverLocation(
            @PathVariable Long rideId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        return ResponseEntity.ok(rideService.updateDriverLocation(rideId, latitude, longitude));
    }

    @DeleteMapping("/{rideId}")
    public ResponseEntity<RideRequest> cancelRide(
            @PathVariable Long rideId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(rideService.cancelRide(rideId, userId));
    }
}

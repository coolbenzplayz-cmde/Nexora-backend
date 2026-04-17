package org.example.nexora.ride;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, Long> {
    Page<RideRequest> findByUserId(Long userId, Pageable pageable);
    Page<RideRequest> findByDriverId(Long driverId, Pageable pageable);
    List<RideRequest> findByStatus(RideRequest.RideStatus status);
    Page<RideRequest> findByUserIdAndStatus(Long userId, RideRequest.RideStatus status, Pageable pageable);
}

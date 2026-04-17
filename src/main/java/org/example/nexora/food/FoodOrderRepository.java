package org.example.nexora.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    Page<FoodOrder> findByUserId(Long userId, Pageable pageable);
    Page<FoodOrder> findByRestaurantId(Long restaurantId, Pageable pageable);
    Page<FoodOrder> findByDriverId(Long driverId, Pageable pageable);
    List<FoodOrder> findByStatus(FoodOrder.OrderStatus status);
    Page<FoodOrder> findByUserIdAndStatus(Long userId, FoodOrder.OrderStatus status, Pageable pageable);
}

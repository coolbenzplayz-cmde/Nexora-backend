package org.example.nexora.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
    Page<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, String category, Pageable pageable);
    List<MenuItem> findByRestaurantIdAndCategoryAndIsAvailableTrue(Long restaurantId, String category);
}

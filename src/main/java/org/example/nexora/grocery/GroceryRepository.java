package org.example.nexora.grocery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroceryRepository extends JpaRepository<GroceryItem, Long> {
    Page<GroceryItem> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);
    Page<GroceryItem> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    List<GroceryItem> findByStoreId(Long storeId);
    Page<GroceryItem> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);
    List<GroceryItem> findByIsAvailableTrue();
    
    @Query("SELECT DISTINCT g.category FROM GroceryItem g WHERE g.isAvailable = true")
    List<String> findAllCategories();
}

package org.example.nexora.marketplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND " +
           "(p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 " +
           "ORDER BY p.rating DESC")
    List<Product> findTopRatedProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 " +
           "ORDER BY p.createdAt DESC")
    List<Product> findRecentProducts(Pageable pageable);
}

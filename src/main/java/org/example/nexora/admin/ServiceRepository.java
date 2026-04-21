package org.example.nexora.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findByName(String name);

    List<Service> findByCategory(String category);

    List<Service> findByStatus(ServiceStatus status);

    List<Service> findByIsPublic(Boolean isPublic);

    Page<Service> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Service> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    @Query("SELECT s FROM Service s WHERE " +
           "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR LOWER(s.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:isPublic IS NULL OR s.isPublic = :isPublic)")
    Page<Service> findByFilters(@Param("name") String name,
                                @Param("category") String category,
                                @Param("status") ServiceStatus status,
                                @Param("isPublic") Boolean isPublic,
                                Pageable pageable);

    @Query("SELECT DISTINCT s.category FROM Service s")
    List<String> findAllCategories();

    @Query("SELECT COUNT(s) FROM Service s WHERE s.status = :status")
    long countByStatus(@Param("status") ServiceStatus status);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.category = :category")
    long countByCategory(@Param("category") String category);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.isPublic = :isPublic")
    long countByIsPublic(@Param("isPublic") Boolean isPublic);
}

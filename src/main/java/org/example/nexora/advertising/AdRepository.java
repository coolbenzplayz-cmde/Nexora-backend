package org.example.nexora.advertising;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    Page<Ad> findByAdvertiserId(String advertiserId, Pageable pageable);

    Page<Ad> findByStatus(Ad.AdStatus status, Pageable pageable);

    @Query("SELECT a FROM Ad a WHERE a.status = 'ACTIVE' AND a.startDate <= CURRENT_TIMESTAMP AND (a.endDate IS NULL OR a.endDate >= CURRENT_TIMESTAMP)")
    List<Ad> findActiveAds();

    @Query("SELECT a FROM Ad a WHERE a.advertiserId = :advertiserId AND a.status = :status")
    Page<Ad> findByAdvertiserAndStatus(
            @Param("advertiserId") String advertiserId,
            @Param("status") Ad.AdStatus status,
            Pageable pageable);

    @Query("SELECT SUM(a.spent) FROM Ad a WHERE a.advertiserId = :advertiserId")
    Double calculateTotalSpendByAdvertiser(@Param("advertiserId") String advertiserId);

    @Query("SELECT SUM(a.impressions) FROM Ad a WHERE a.advertiserId = :advertiserId")
    Long calculateTotalImpressionsByAdvertiser(@Param("advertiserId") String advertiserId);

    @Query("SELECT SUM(a.clicks) FROM Ad a WHERE a.advertiserId = :advertiserId")
    Long calculateTotalClicksByAdvertiser(@Param("advertiserId") String advertiserId);
}

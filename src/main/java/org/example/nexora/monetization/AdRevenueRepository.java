package org.example.nexora.monetization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdRevenueRepository extends JpaRepository<AdRevenue, Long> {

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.creatorId = :creatorId ORDER BY ar.revenueDate DESC")
    Page<AdRevenue> findByCreatorIdOrderByRevenueDateDesc(@Param("creatorId") Long creatorId, Pageable pageable);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.creatorId = :creatorId ORDER BY ar.revenueDate DESC")
    List<AdRevenue> findByCreatorIdOrderByRevenueDateDesc(@Param("creatorId") Long creatorId);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.creatorId = :creatorId AND ar.status = :status ORDER BY ar.revenueDate DESC")
    List<AdRevenue> findByCreatorIdAndStatus(@Param("creatorId") Long creatorId, @Param("status") AdRevenue.RevenueStatus status);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.videoId = :videoId ORDER BY ar.revenueDate DESC")
    List<AdRevenue> findByVideoIdOrderByRevenueDateDesc(@Param("videoId") Long videoId);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.adType = :adType ORDER BY ar.revenueDate DESC")
    Page<AdRevenue> findByAdTypeOrderByRevenueDateDesc(@Param("adType") AdRevenue.AdType adType, Pageable pageable);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.status = :status ORDER BY ar.revenueDate DESC")
    Page<AdRevenue> findByStatusOrderByRevenueDateDesc(@Param("status") AdRevenue.RevenueStatus status, Pageable pageable);

    @Query("SELECT SUM(ar.revenueAmount) FROM AdRevenue ar WHERE ar.creatorId = :creatorId AND ar.status = :verifiedStatus")
    BigDecimal sumVerifiedRevenueByCreatorId(@Param("creatorId") Long creatorId, @Param("verifiedStatus") AdRevenue.RevenueStatus verifiedStatus);

    @Query("SELECT SUM(ar.revenueAmount) FROM AdRevenue ar WHERE ar.status = :verifiedStatus")
    BigDecimal sumTotalRevenue(@Param("verifiedStatus") AdRevenue.RevenueStatus verifiedStatus);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.revenueDate BETWEEN :startDate AND :endDate ORDER BY ar.revenueDate DESC")
    Page<AdRevenue> findByRevenueDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.creatorId = :creatorId AND ar.revenueDate BETWEEN :startDate AND :endDate ORDER BY ar.revenueDate DESC")
    Page<AdRevenue> findByCreatorIdAndRevenueDateBetween(@Param("creatorId") Long creatorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT COUNT(ar) FROM AdRevenue ar WHERE ar.creatorId = :creatorId")
    long countByCreatorId(@Param("creatorId") Long creatorId);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.isPaid = false ORDER BY ar.revenueDate ASC")
    List<AdRevenue> findAllUnpaidRevenue();

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.creatorId = :creatorId AND ar.isPaid = false AND ar.status = :verifiedStatus")
    List<AdRevenue> findUnpaidVerifiedRevenueByCreatorId(@Param("creatorId") Long creatorId, @Param("verifiedStatus") AdRevenue.RevenueStatus verifiedStatus);

    @Query("SELECT SUM(ar.revenueAmount) FROM AdRevenue ar WHERE ar.creatorId = :creatorId AND ar.isPaid = false AND ar.status = :verifiedStatus")
    BigDecimal sumUnpaidVerifiedRevenueByCreatorId(@Param("creatorId") Long creatorId, @Param("verifiedStatus") AdRevenue.RevenueStatus verifiedStatus);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.impressions > 0 ORDER BY ar.impressions DESC")
    Page<AdRevenue> findTopPerformingAdsByImpressions(Pageable pageable);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.clicks > 0 ORDER BY ar.clicks DESC")
    Page<AdRevenue> findTopPerformingAdsByClicks(Pageable pageable);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.revenueAmount > 0 ORDER BY ar.revenueAmount DESC")
    Page<AdRevenue> findTopPerformingAdsByRevenue(Pageable pageable);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.status = :pendingStatus ORDER BY ar.revenueDate ASC")
    List<AdRevenue> findPendingRevenue(@Param("pendingStatus") AdRevenue.RevenueStatus pendingStatus);

    @Query("SELECT ar FROM AdRevenue ar WHERE ar.adCampaignId = :campaignId ORDER BY ar.revenueDate DESC")
    List<AdRevenue> findByAdCampaignId(@Param("campaignId") String campaignId);
}

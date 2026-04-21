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
public interface CreatorEarningsRepository extends JpaRepository<CreatorEarnings, Long> {

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId ORDER BY ce.earningsDate DESC")
    Page<CreatorEarnings> findByCreatorIdOrderByEarningsDateDesc(@Param("creatorId") Long creatorId, Pageable pageable);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId ORDER BY ce.earningsDate DESC")
    List<CreatorEarnings> findByCreatorIdOrderByEarningsDateDesc(@Param("creatorId") Long creatorId);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId AND ce.isPaid = false ORDER BY ce.earningsDate DESC")
    List<CreatorEarnings> findUnpaidEarningsByCreatorId(@Param("creatorId") Long creatorId);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.videoId = :videoId ORDER BY ce.earningsDate DESC")
    List<CreatorEarnings> findByVideoIdOrderByEarningsDateDesc(@Param("videoId") Long videoId);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.earningsType = :earningsType ORDER BY ce.earningsDate DESC")
    Page<CreatorEarnings> findByEarningsTypeOrderByEarningsDateDesc(@Param("earningsType") CreatorEarnings.EarningsType earningsType, Pageable pageable);

    @Query("SELECT SUM(ce.amount) FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId AND ce.earningsType != :refundType")
    BigDecimal sumEarningsByCreatorId(@Param("creatorId") Long creatorId, @Param("refundType") CreatorEarnings.EarningsType refundType);

    @Query("SELECT SUM(ce.amount) FROM CreatorEarnings ce WHERE ce.earningsType != :refundType")
    BigDecimal sumTotalEarnings(@Param("refundType") CreatorEarnings.EarningsType refundType);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.earningsDate BETWEEN :startDate AND :endDate ORDER BY ce.earningsDate DESC")
    Page<CreatorEarnings> findByEarningsDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId AND ce.earningsDate BETWEEN :startDate AND :endDate ORDER BY ce.earningsDate DESC")
    Page<CreatorEarnings> findByCreatorIdAndEarningsDateBetween(@Param("creatorId") Long creatorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT COUNT(ce) FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId")
    long countByCreatorId(@Param("creatorId") Long creatorId);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.isPaid = false ORDER BY ce.earningsDate ASC")
    List<CreatorEarnings> findAllUnpaidEarnings();

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId AND ce.isPaid = false AND ce.earningsType != :refundType")
    List<CreatorEarnings> findUnpaidEarningsByCreatorIdExcludingRefunds(@Param("creatorId") Long creatorId, @Param("refundType") CreatorEarnings.EarningsType refundType);

    @Query("SELECT SUM(ce.amount) FROM CreatorEarnings ce WHERE ce.creatorId = :creatorId AND ce.isPaid = false AND ce.earningsType != :refundType")
    BigDecimal sumUnpaidEarningsByCreatorId(@Param("creatorId") Long creatorId, @Param("refundType") CreatorEarnings.EarningsType refundType);

    @Query("SELECT ce FROM CreatorEarnings ce WHERE ce.description LIKE %:keyword% ORDER BY ce.earningsDate DESC")
    Page<CreatorEarnings> searchByDescription(@Param("keyword") String keyword, Pageable pageable);
}
